package com.dtp.core.spring;

import com.dtp.common.entity.DtpExecutorProps;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.BeanUtil;
import com.dtp.core.reject.RejectHandlerGetter;
import com.dtp.core.support.ExecutorType;
import com.dtp.core.support.TaskQueue;
import com.dtp.core.support.wrapper.TaskWrappers;
import com.dtp.core.thread.EagerDtpExecutor;
import com.dtp.core.thread.NamedThreadFactory;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static com.dtp.common.constant.DynamicTpConst.ALLOW_CORE_THREAD_TIMEOUT;
import static com.dtp.common.constant.DynamicTpConst.AWAIT_TERMINATION_SECONDS;
import static com.dtp.common.constant.DynamicTpConst.NOTIFY_ENABLED;
import static com.dtp.common.constant.DynamicTpConst.NOTIFY_ITEMS;
import static com.dtp.common.constant.DynamicTpConst.PLATFORM_IDS;
import static com.dtp.common.constant.DynamicTpConst.PRE_START_ALL_CORE_THREADS;
import static com.dtp.common.constant.DynamicTpConst.QUEUE_TIMEOUT;
import static com.dtp.common.constant.DynamicTpConst.RUN_TIMEOUT;
import static com.dtp.common.constant.DynamicTpConst.TASK_WRAPPERS;
import static com.dtp.common.constant.DynamicTpConst.THREAD_POOL_ALIAS_NAME;
import static com.dtp.common.constant.DynamicTpConst.THREAD_POOL_NAME;
import static com.dtp.common.constant.DynamicTpConst.WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN;
import static com.dtp.common.em.QueueTypeEnum.buildLbq;
import static com.dtp.common.entity.NotifyItem.mergeAllNotifyItems;

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
        if (CollectionUtils.isEmpty(executors)) {
            log.warn("DynamicTp registrar, no executors are configured.");
            return;
        }

        executors.forEach(e -> {
            Class<?> executorTypeClass = ExecutorType.getClass(e.getExecutorType());
            Map<String, Object> propertyValues = buildPropertyValues(e);
            Object[] args = buildConstructorArgs(executorTypeClass, e);
            BeanUtil.registerIfAbsent(registry, e.getThreadPoolName(), executorTypeClass, propertyValues, args);
        });
    }

    private Map<String, Object> buildPropertyValues(DtpExecutorProps props) {
        Map<String, Object> propertyValues = Maps.newHashMap();
        propertyValues.put(THREAD_POOL_NAME, props.getThreadPoolName());
        propertyValues.put(THREAD_POOL_ALIAS_NAME, props.getThreadPoolAliasName());
        propertyValues.put(ALLOW_CORE_THREAD_TIMEOUT, props.isAllowCoreThreadTimeOut());
        propertyValues.put(WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN, props.isWaitForTasksToCompleteOnShutdown());
        propertyValues.put(AWAIT_TERMINATION_SECONDS, props.getAwaitTerminationSeconds());
        propertyValues.put(PRE_START_ALL_CORE_THREADS, props.isPreStartAllCoreThreads());
        propertyValues.put(RUN_TIMEOUT, props.getRunTimeout());
        propertyValues.put(QUEUE_TIMEOUT, props.getQueueTimeout());

        val notifyItems = mergeAllNotifyItems(props.getNotifyItems());
        propertyValues.put(NOTIFY_ITEMS, notifyItems);
        propertyValues.put(PLATFORM_IDS, props.getPlatformIds());
        propertyValues.put(NOTIFY_ENABLED, props.isNotifyEnabled());

        val taskWrappers = TaskWrappers.getInstance().getByNames(props.getTaskWrapperNames());
        propertyValues.put(TASK_WRAPPERS, taskWrappers);

        return propertyValues;
    }

    private Object[] buildConstructorArgs(Class<?> clazz, DtpExecutorProps props) {
        BlockingQueue<Runnable> taskQueue;
        if (clazz.equals(EagerDtpExecutor.class)) {
            taskQueue = new TaskQueue(props.getQueueCapacity());
        } else {
            taskQueue = buildLbq(props.getQueueType(),
                    props.getQueueCapacity(),
                    props.isFair(),
                    props.getMaxFreeMemory());
        }

        return new Object[] {
                props.getCorePoolSize(),
                props.getMaximumPoolSize(),
                props.getKeepAliveTime(),
                props.getUnit(),
                taskQueue,
                new NamedThreadFactory(props.getThreadNamePrefix()),
                RejectHandlerGetter.buildRejectedHandler(props.getRejectedHandlerType())
        };
    }
}
