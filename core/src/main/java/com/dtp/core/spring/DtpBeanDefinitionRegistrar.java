package com.dtp.core.spring;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.properties.ThreadPoolProperties;
import com.dtp.common.util.BeanUtil;
import com.dtp.core.reject.RejectHandlerGetter;
import com.dtp.core.support.ExecutorType;
import com.dtp.core.support.PropertiesBinder;
import com.dtp.core.support.TaskQueue;
import com.dtp.core.support.wrapper.TaskWrappers;
import com.dtp.core.thread.EagerDtpExecutor;
import com.dtp.core.thread.NamedThreadFactory;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static com.dtp.common.constant.DynamicTpConst.*;
import static com.dtp.common.dto.NotifyItem.mergeAllNotifyItems;
import static com.dtp.common.em.QueueTypeEnum.buildLbq;

/**
 * DtpBeanDefinitionRegistrar related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Slf4j
public class DtpBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        DtpProperties dtpProperties = new DtpProperties();
        PropertiesBinder.bindDtpProperties(environment, dtpProperties);
        val executors = dtpProperties.getExecutors();
        if (CollUtil.isEmpty(executors)) {
            log.warn("DynamicTp registrar, no executors are configured.");
            return;
        }

        executors.forEach(x -> {
            Class<?> executorTypeClass = ExecutorType.getClass(x.getExecutorType());
            Map<String, Object> properties = buildPropertyValues(x);
            Object[] args = buildConstructorArgs(executorTypeClass, x);
            BeanUtil.registerIfAbsent(registry, x.getThreadPoolName(), executorTypeClass, properties, args);
        });
    }

    private Map<String, Object> buildPropertyValues(ThreadPoolProperties tpp) {
        Map<String, Object> properties = Maps.newHashMap();
        properties.put(THREAD_POOL_NAME, tpp.getThreadPoolName());
        properties.put(THREAD_POOL_ALIAS_NAME, tpp.getThreadPoolAliasName());
        properties.put(ALLOW_CORE_THREAD_TIMEOUT, tpp.isAllowCoreThreadTimeOut());
        properties.put(WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN, tpp.isWaitForTasksToCompleteOnShutdown());
        properties.put(AWAIT_TERMINATION_SECONDS, tpp.getAwaitTerminationSeconds());
        properties.put(PRE_START_ALL_CORE_THREADS, tpp.isPreStartAllCoreThreads());
        properties.put(RUN_TIMEOUT, tpp.getRunTimeout());
        properties.put(QUEUE_TIMEOUT, tpp.getQueueTimeout());

        val notifyItems = mergeAllNotifyItems(tpp.getNotifyItems());
        properties.put(NOTIFY_ITEMS, notifyItems);
        properties.put(NOTIFY_ENABLED, tpp.isNotifyEnabled());

        val taskWrappers = TaskWrappers.getInstance().getByNames(tpp.getTaskWrapperNames());
        properties.put(TASK_WRAPPERS, taskWrappers);

        return properties;
    }

    private Object[] buildConstructorArgs(Class<?> clazz, ThreadPoolProperties tpp) {

        BlockingQueue<Runnable> taskQueue;
        if (clazz.equals(EagerDtpExecutor.class)) {
            taskQueue = new TaskQueue(tpp.getQueueCapacity(), tpp.getMaxFreeMemory() * M_1);
        } else {
            taskQueue = buildLbq(tpp.getQueueType(), tpp.getQueueCapacity(), tpp.isFair(), tpp.getMaxFreeMemory());
        }

        return new Object[] {
                tpp.getCorePoolSize(),
                tpp.getMaximumPoolSize(),
                tpp.getKeepAliveTime(),
                tpp.getUnit(),
                taskQueue,
                new NamedThreadFactory(tpp.getThreadNamePrefix()),
                RejectHandlerGetter.buildRejectedHandler(tpp.getRejectedHandlerType())
        };
    }
}
