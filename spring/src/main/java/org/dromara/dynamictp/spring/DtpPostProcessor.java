/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.spring;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.dromara.dynamictp.common.plugin.DtpInterceptorRegistry;
import org.dromara.dynamictp.common.util.ConstructorUtil;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.executor.eager.EagerDtpExecutor;
import org.dromara.dynamictp.core.executor.eager.TaskQueue;
import org.dromara.dynamictp.core.support.DynamicTp;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.proxy.ScheduledThreadPoolExecutorProxy;
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.core.support.proxy.VirtualThreadExecutorProxy;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrappers;
import org.dromara.dynamictp.spring.support.SimpleAsyncTaskExecutorAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.type.MethodMetadata;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.dromara.dynamictp.core.support.DtpLifecycleSupport.shutdownGracefulAsync;

/**
 * BeanPostProcessor that handles all related beans managed by Spring.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
@SuppressWarnings("all")
public class DtpPostProcessor implements BeanPostProcessor, BeanFactoryAware, EnvironmentAware, PriorityOrdered {

    private static final String REGISTER_SOURCE = "beanPostProcessor";

    private DefaultListableBeanFactory beanFactory;

    /**
     * Cached via {@link EnvironmentAware}; reading it inside a BeanPostProcessor through
     * {@code beanFactory.getBean(Environment.class)} would force early bean initialization and
     * disturb the bean-creation order, so we hold the reference injected by Spring instead.
     */
    private Environment environment;

    /**
     * Property key that enables virtual threads in Spring Boot 3.
     */
    private static final String SPRING_THREADS_VIRTUAL_ENABLED = "spring.threads.virtual.enabled";

    /**
     * Spring's internal marker field, non-null when a SimpleAsyncTaskExecutor spawns virtual
     * threads. Added in Spring Framework 6.1, resolved once and {@code null} on older versions.
     */
    private static final Field VIRTUAL_THREAD_DELEGATE_FIELD =
            FieldUtils.getField(SimpleAsyncTaskExecutor.class, "virtualThreadDelegate", true);

    /**
     * Spring's internal field holding a user configured task decorator.
     */
    private static final String TASK_DECORATOR_FIELD = "taskDecorator";

    /**
     * Compatible with lower versions of Spring.
     *
     * @param bean the new bean instance
     * @param beanName the name of the bean
     * @return the bean instance to use
     * @throws BeansException in case of errors
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof VirtualThreadExecutorProxy) {
            return registerAndReturnVirtualProxy((VirtualThreadExecutorProxy) bean, beanName);
        }
        // Spring Boot 3, when spring.threads.virtual.enabled=true, registers the
        // applicationTaskExecutor as a SimpleAsyncTaskExecutor (with virtualThreads=true),
        // which is neither ThreadPoolExecutor nor ThreadPoolTaskExecutor. Catch it here so
        // dtp can still observe / enhance it without touching the original bean.
        if (bean instanceof SimpleAsyncTaskExecutor
                && isVirtualThreadExecutor((SimpleAsyncTaskExecutor) bean)) {
            return registerAndReturnVirtual(bean, beanName);
        }
        if (!(bean instanceof ThreadPoolExecutor) && !(bean instanceof ThreadPoolTaskExecutor)) {
            return bean;
        }
        if (bean instanceof DtpExecutor) {
            return registerAndReturnDtp(bean);
        }
        // register juc ThreadPoolExecutor or ThreadPoolTaskExecutor
        return registerAndReturnCommon(bean, beanName);
    }

    /**
     * Whether the given {@link SimpleAsyncTaskExecutor} should be managed as a
     * virtual-thread (thread-per-task) executor.
     *
     * <p>A {@code SimpleAsyncTaskExecutor} created with {@code setVirtualThreads(true)} holds a
     * non-null {@code virtualThreadDelegate}, so the instance state is the only reliable signal
     * and it also covers executors that opt in manually while the global property is off.
     * Reading the field requires Spring Framework 6.1+, hence it is resolved once; on older
     * versions the field does not exist and the global property is the only hint left (dtp then
     * trusts the user's intent, the executor may in fact run on platform threads).</p>
     *
     * <p>The property must never be used as an additional signal while the field is available:
     * with {@code spring.threads.virtual.enabled=true} every single
     * {@code SimpleAsyncTaskExecutor} bean would be taken over, including the ones that run on
     * platform threads, and would then be reported as a queue-less virtual pool.</p>
     */
    private boolean isVirtualThreadExecutor(SimpleAsyncTaskExecutor executor) {
        if (executor instanceof TaskScheduler) {
            // SimpleAsyncTaskScheduler extends SimpleAsyncTaskExecutor. A scheduler is not an
            // application task executor: taking it over would add a pool that cannot be tuned
            // and would replace the decorator slot used by the scheduling infrastructure.
            log.debug("DynamicTp, skip managing task scheduler as a virtual thread executor: {}",
                    executor.getClass().getName());
            return false;
        }
        if (Objects.nonNull(VIRTUAL_THREAD_DELEGATE_FIELD)) {
            return usesVirtualThreads(executor);
        }
        return isVirtualThreadEnabled();
    }

    private boolean usesVirtualThreads(SimpleAsyncTaskExecutor executor) {
        return Objects.nonNull(ReflectionUtil.getFieldValue(VIRTUAL_THREAD_DELEGATE_FIELD, executor));
    }

    /**
     * Whether Spring Boot virtual threads are enabled.
     */
    private boolean isVirtualThreadEnabled() {
        return environment != null
                && environment.getProperty(SPRING_THREADS_VIRTUAL_ENABLED, Boolean.class, false);
    }

    /**
     * Register a Spring Boot virtual thread executor (SimpleAsyncTaskExecutor) into dtp
     * without replacing the original bean. Only observe / enhance, do not swap the bean
     * reference held by Spring.
     */
    private Object registerAndReturnVirtual(Object bean, String beanName) {
        String poolName = findDtpAnnoValue(beanName)
                .filter(StringUtils::isNotBlank)
                .orElse(beanName);
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = (SimpleAsyncTaskExecutor) bean;
        SimpleAsyncTaskExecutorAdapter adapter = new SimpleAsyncTaskExecutorAdapter(simpleAsyncTaskExecutor);
        VirtualThreadExecutorProxy proxy = new VirtualThreadExecutorProxy(adapter);
        proxy.setThreadPoolName(poolName);
        if (!tryWrapSimpleAsyncTaskDecorator(poolName, simpleAsyncTaskExecutor, proxy)) {
            // Without the decorator hook every task submitted through the bean bypasses dtp:
            // registering anyway would add a pool that reports zeros forever.
            log.warn("DynamicTp, cannot enhance executor [{}], skip managing it.", poolName);
            return bean;
        }
        warnIfTasksCanBeRejectedOutsideDtp(poolName, simpleAsyncTaskExecutor);
        DtpRegistry.registerExecutor(new ExecutorWrapper(poolName, proxy), REGISTER_SOURCE);
        return bean;
    }

    /**
     * A {@code SimpleAsyncTaskExecutor} with a concurrency limit throttles or rejects tasks
     * inside its own {@code execute}, i.e. before / around the decorator dtp hooks into. Those
     * tasks are invisible to dtp, so say it once at startup instead of letting users wonder why
     * the reject count stays at zero.
     */
    private void warnIfTasksCanBeRejectedOutsideDtp(String poolName, SimpleAsyncTaskExecutor executor) {
        if (executor.getConcurrencyLimit() != SimpleAsyncTaskExecutor.UNBOUNDED_CONCURRENCY) {
            log.warn("DynamicTp, executor [{}] has a concurrency limit of [{}], tasks throttled or "
                            + "rejected by the executor itself are not reflected in the metrics of this pool.",
                    poolName, executor.getConcurrencyLimit());
        }
    }

    private Object registerAndReturnVirtualProxy(VirtualThreadExecutorProxy proxy, String beanName) {
        proxy.setThreadPoolName(beanName);
        DtpRegistry.registerExecutor(new ExecutorWrapper(beanName, proxy), REGISTER_SOURCE);
        return proxy;
    }

    private Object registerAndReturnDtp(Object bean) {
        DtpExecutor dtpExecutor = (DtpExecutor) bean;
        Object[] args = ConstructorUtil.buildTpExecutorConstructorArgs(dtpExecutor);
        Class<?>[] argTypes = ConstructorUtil.buildTpExecutorConstructorArgTypes();
        Set<String> pluginNames = dtpExecutor.getPluginNames();

        val enhancedBean = (DtpExecutor) DtpInterceptorRegistry.plugin(bean, pluginNames, argTypes, args);
        if (enhancedBean instanceof EagerDtpExecutor) {
            ((TaskQueue) enhancedBean.getQueue()).setExecutor((EagerDtpExecutor) enhancedBean);
        }
        DtpRegistry.registerExecutor(ExecutorWrapper.of(enhancedBean), REGISTER_SOURCE);
        return enhancedBean;
    }

    private Object registerAndReturnCommon(Object bean, String beanName) {
        Optional<String> dtpAnnoValue = findDtpAnnoValue(beanName);
        if (!dtpAnnoValue.isPresent()) {
            return bean;
        }
        String poolName = StringUtils.isNotBlank(dtpAnnoValue.get()) ? dtpAnnoValue.get() : beanName;
        return doRegisterAndReturnCommon(bean, poolName);
    }

    /**
     * Resolve the {@link DynamicTp} annotation value of the given bean.
     *
     * @param beanName the bean name
     * @return empty when the bean is not annotated at all, otherwise the (possibly blank)
     *         annotation value
     */
    private Optional<String> findDtpAnnoValue(String beanName) {
        try {
            DynamicTp dynamicTp = beanFactory.findAnnotationOnBean(beanName, DynamicTp.class);
            if (Objects.nonNull(dynamicTp)) {
                return Optional.of(dynamicTp.value());
            }
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            if (!(beanDefinition instanceof AnnotatedBeanDefinition)) {
                return Optional.empty();
            }
            AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
            MethodMetadata methodMetadata = (MethodMetadata) annotatedBeanDefinition.getSource();
            if (Objects.isNull(methodMetadata) || !methodMetadata.isAnnotated(DynamicTp.class.getName())) {
                return Optional.empty();
            }
            return Optional.of(Optional.ofNullable(methodMetadata.getAnnotationAttributes(DynamicTp.class.getName()))
                    .orElse(Collections.emptyMap())
                    .getOrDefault("value", "")
                    .toString());
        } catch (NoSuchBeanDefinitionException e) {
            log.debug("There is no bean definition with the given name {}", beanName, e);
            return Optional.empty();
        }
    }

    private Object doRegisterAndReturnCommon(Object bean, String poolName) {
        if (bean instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor poolTaskExecutor = (ThreadPoolTaskExecutor) bean;
            ThreadPoolExecutor originExecutor = poolTaskExecutor.getThreadPoolExecutor();
            val proxy = new ThreadPoolExecutorProxy(originExecutor);
            // The origin executor is only abandoned once the swap succeeded, otherwise the bean
            // would be left holding an executor that dtp just shut down.
            if (!ReflectionUtil.setFieldValue("threadPoolExecutor", bean, proxy)) {
                log.warn("DynamicTp, cannot replace the inner executor of ThreadPoolTaskExecutor [{}], "
                        + "skip managing it.", poolName);
                return bean;
            }
            tryWrapTaskDecorator(poolName, poolTaskExecutor, proxy);
            shutdownGracefulAsync(originExecutor, poolName, 0);
            DtpRegistry.registerExecutor(new ExecutorWrapper(poolName, proxy), REGISTER_SOURCE);
            return bean;
        }
        Executor proxy;
        if (bean instanceof ScheduledThreadPoolExecutor) {
            proxy = newScheduledTpProxy(poolName, (ScheduledThreadPoolExecutor) bean);
        } else {
            proxy = newProxy(poolName, (ThreadPoolExecutor) bean);
        }
        DtpRegistry.registerExecutor(new ExecutorWrapper(poolName, proxy), REGISTER_SOURCE);
        return proxy;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof DefaultListableBeanFactory)) {
            throw new IllegalArgumentException("DynamicTp requires a DefaultListableBeanFactory, but got: "
                    + beanFactory.getClass().getName());
        }
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private ThreadPoolExecutorProxy newProxy(String name, ThreadPoolExecutor originExecutor) {
        val proxy = new ThreadPoolExecutorProxy(originExecutor);
        shutdownGracefulAsync(originExecutor, name, 0);
        return proxy;
    }

    private ScheduledThreadPoolExecutorProxy newScheduledTpProxy(String name, ScheduledThreadPoolExecutor originExecutor) {
        val proxy = new ScheduledThreadPoolExecutorProxy(originExecutor);
        shutdownGracefulAsync(originExecutor, name, 0);
        return proxy;
    }

    private void tryWrapTaskDecorator(String poolName, ThreadPoolTaskExecutor poolTaskExecutor, ThreadPoolExecutorProxy proxy) {
        Object taskDecorator = ReflectionUtil.getFieldValue("taskDecorator", poolTaskExecutor);
        if (Objects.isNull(taskDecorator)) {
            return;
        }
        TaskWrapper taskWrapper = (taskDecorator instanceof TaskWrapper) ? (TaskWrapper) taskDecorator : new TaskWrapper() {
            @Override
            public String name() {
                return poolName + "#taskDecorator";
            }

            @Override
            public Runnable wrap(Runnable runnable) {
                return ((TaskDecorator) taskDecorator).decorate(runnable);
            }
        };
        // The decorating executor created by ThreadPoolTaskExecutor is replaced by the proxy and
        // shut down, so the decorator only survives as a task wrapper. Registering it as the
        // internal one keeps a config refresh (which resets the wrappers to taskWrapperNames)
        // from dropping it.
        proxy.setInternalTaskWrapper(taskWrapper);
        TaskWrappers.getInstance().register(taskWrapper);
    }

    /**
     * Take over the {@code TaskDecorator} slot of the given executor, which is the only hook
     * that lets dtp see the tasks submitted through the bean itself.
     *
     * @return false if the hook could not be installed, in which case the executor must not be
     *         managed at all
     */
    private boolean tryWrapSimpleAsyncTaskDecorator(String poolName,
                                                    SimpleAsyncTaskExecutor executor,
                                                    VirtualThreadExecutorProxy proxy) {
        // dtp installs its own decorator below, so a user configured one must be carried over
        // as a task wrapper first. If the field cannot be located (Spring internals changed),
        // bail out instead of silently dropping the user's decorator.
        if (Objects.isNull(ReflectionUtil.getField(SimpleAsyncTaskExecutor.class, TASK_DECORATOR_FIELD))) {
            log.warn("DynamicTp cannot read field [{}] of SimpleAsyncTaskExecutor, skip enhancing task decorator, "
                    + "tpName: {}", TASK_DECORATOR_FIELD, poolName);
            return false;
        }
        Object taskDecorator = ReflectionUtil.getFieldValue(TASK_DECORATOR_FIELD, executor);
        if (Objects.nonNull(taskDecorator)) {
            TaskWrapper taskWrapper = (taskDecorator instanceof TaskWrapper) ? (TaskWrapper) taskDecorator
                    : new TaskWrapper() {
                        @Override
                        public String name() {
                            return poolName + "#taskDecorator";
                        }

                        @Override
                        public Runnable wrap(Runnable runnable) {
                            return ((TaskDecorator) taskDecorator).decorate(runnable);
                        }
                    };
            // registered as the internal wrapper so a later config refresh, which resets the
            // wrappers to the ones named in taskWrapperNames, cannot drop it
            proxy.setInternalTaskWrapper(taskWrapper);
            TaskWrappers.getInstance().register(taskWrapper);
        }
        // Hooking the decorator keeps the bean itself untouched, but it only sees tasks that
        // Spring hands to the decorator. Rejections raised by the executor bean itself (closed
        // executor, or concurrency limit with rejectTasksWhenLimitReached) are never routed
        // through dtp, so they do not show up in the reject metrics / alarms of this pool.
        // Only submissions that go through DtpRegistry / the wrapper are counted as rejected.
        executor.setTaskDecorator(proxy::decorate);
        return true;
    }
}
