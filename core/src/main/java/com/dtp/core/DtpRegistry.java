package com.dtp.core;

import com.dtp.common.entity.DtpExecutorProps;
import com.dtp.common.entity.TpMainFields;
import com.dtp.common.ex.DtpException;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.queue.MemorySafeLinkedBlockingQueue;
import com.dtp.common.queue.VariableLinkedBlockingQueue;
import com.dtp.core.convert.ExecutorConverter;
import com.dtp.core.notify.manager.NoticeManager;
import com.dtp.core.reject.RejectHandlerGetter;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.support.wrapper.TaskWrapper;
import com.dtp.core.support.wrapper.TaskWrappers;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.support.ExecutorAdapter;
import com.github.dadiyang.equator.Equator;
import com.github.dadiyang.equator.FieldInfo;
import com.github.dadiyang.equator.GetterBaseEquator;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.dtp.common.constant.DynamicTpConst.M_1;
import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;
import static com.dtp.core.notify.manager.NotifyHelper.updateNotifyInfo;
import static java.util.stream.Collectors.toList;

/**
 * Core Registry, which keeps all registered Dynamic ThreadPoolExecutors.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpRegistry implements ApplicationRunner, Ordered {

    /**
     * Maintain all automatically registered and manually registered DtpExecutors.
     */
    private static final Map<String, DtpExecutor> DTP_REGISTRY = new ConcurrentHashMap<>();

    /**
     * Maintain all automatically registered and manually registered JUC ThreadPoolExecutors.
     */
    private static final Map<String, ExecutorWrapper> COMMON_REGISTRY = new ConcurrentHashMap<>();

    private static final Equator EQUATOR = new GetterBaseEquator();

    private static DtpProperties dtpProperties;

    /**
     * Get all DtpExecutor names.
     *
     * @return executor names
     */
    public static List<String> listAllDtpNames() {
        return Lists.newArrayList(DTP_REGISTRY.keySet());
    }

    /**
     * Get all JUC ThreadPoolExecutor names.
     *
     * @return executor name
     */
    public static List<String> listAllCommonNames() {
        return Lists.newArrayList(COMMON_REGISTRY.keySet());
    }

    /**
     * Register a DtpExecutor.
     *
     * @param executor the newly created DtpExecutor instance
     * @param source   the source of the call to register method
     */
    public static void registerDtp(DtpExecutor executor, String source) {
        log.info("DynamicTp register dtpExecutor, source: {}, executor: {}",
                source, ExecutorConverter.convert(executor));
        DTP_REGISTRY.putIfAbsent(executor.getThreadPoolName(), executor);
    }

    /**
     * Register a common ThreadPoolExecutor.
     *
     * @param wrapper the newly created ThreadPoolExecutor wrapper instance
     * @param source  the source of the call to register method
     */
    public static void registerCommon(ExecutorWrapper wrapper, String source) {
        log.info("DynamicTp register commonExecutor, source: {}, executor: {}",
                source, ExecutorConverter.convert(wrapper));
        COMMON_REGISTRY.putIfAbsent(wrapper.getThreadPoolName(), wrapper);
    }

    /**
     * Get Dynamic ThreadPoolExecutor by thread pool name.
     *
     * @param name the name of dynamic thread pool
     * @return the managed DtpExecutor instance
     */
    public static DtpExecutor getDtpExecutor(final String name) {
        val executor = DTP_REGISTRY.get(name);
        if (Objects.isNull(executor)) {
            log.error("Cannot find a specified dtpExecutor, name: {}", name);
            throw new DtpException("Cannot find a specified dtpExecutor, name: " + name);
        }
        return executor;
    }

    /**
     * Get common ThreadPoolExecutor by name.
     *
     * @param name the name of thread pool
     * @return the managed ExecutorWrapper instance
     */
    public static ExecutorWrapper getCommonExecutor(final String name) {
        val executor = COMMON_REGISTRY.get(name);
        if (Objects.isNull(executor)) {
            log.error("Cannot find a specified commonExecutor, name: {}", name);
            throw new DtpException("Cannot find a specified commonExecutor, name: " + name);
        }
        return executor;
    }

    /**
     * Refresh while the listening configuration changed.
     *
     * @param dtpProperties the main properties that maintain by config center
     */
    public static void refresh(DtpProperties dtpProperties) {
        if (Objects.isNull(dtpProperties) || CollectionUtils.isEmpty(dtpProperties.getExecutors())) {
            log.warn("DynamicTp refresh, empty threadPool properties.");
            return;
        }
        dtpProperties.getExecutors().forEach(x -> {
            if (StringUtils.isBlank(x.getThreadPoolName())) {
                log.warn("DynamicTp refresh, threadPoolName must not be empty.");
                return;
            }

            // First look it up in the DTP_REGISTRY
            val dtpExecutor = DTP_REGISTRY.get(x.getThreadPoolName());
            if (Objects.nonNull(dtpExecutor)) {
                refresh(ExecutorWrapper.of(dtpExecutor), x);
                return;
            }

            // And then look it up in the COMMON_REGISTRY
            val executorWrapper = COMMON_REGISTRY.get(x.getThreadPoolName());
            if (Objects.nonNull(executorWrapper)) {
                refresh(executorWrapper, x);
                return;
            }
            log.warn("DynamicTp refresh, cannot find specified executor, name: {}.", x.getThreadPoolName());
        });
    }

    private static void refresh(ExecutorWrapper executorWrapper, DtpExecutorProps props) {

        if (props.coreParamIsInValid()) {
            log.error("DynamicTp refresh, invalid parameters exist, properties: {}", props);
            return;
        }
        TpMainFields oldFields = ExecutorConverter.convert(executorWrapper);
        doRefresh(executorWrapper, props);
        TpMainFields newFields = ExecutorConverter.convert(executorWrapper);
        if (oldFields.equals(newFields)) {
            log.debug("DynamicTp refresh, main properties of [{}] have not changed.",
                    executorWrapper.getThreadPoolName());
            return;
        }

        List<String> diffKeys = EQUATOR.getDiffFields(oldFields, newFields)
                .stream().map(FieldInfo::getFieldName).collect(toList());
        NoticeManager.doNoticeAsync(executorWrapper, oldFields, diffKeys);
        log.info("DynamicTp refresh, name: [{}], changed keys: {}, corePoolSize: [{}], maxPoolSize: [{}]," +
                        " queueType: [{}], queueCapacity: [{}], keepAliveTime: [{}], rejectedType: [{}]," +
                        " allowsCoreThreadTimeOut: [{}]", executorWrapper.getThreadPoolName(), diffKeys,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getCorePoolSize(), newFields.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getMaxPoolSize(), newFields.getMaxPoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getQueueType(), newFields.getQueueType()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getQueueCapacity(), newFields.getQueueCapacity()),
                String.format("%ss => %ss", oldFields.getKeepAliveTime(), newFields.getKeepAliveTime()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getRejectType(), newFields.getRejectType()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.isAllowCoreThreadTimeOut(),
                        newFields.isAllowCoreThreadTimeOut()));
    }

    private static void doRefresh(ExecutorWrapper executorWrapper, DtpExecutorProps props) {
        ExecutorAdapter<?> executor = executorWrapper.getExecutor();
        doRefreshPoolSize(executor, props);
        if (!Objects.equals(executor.getKeepAliveTime(props.getUnit()), props.getKeepAliveTime())) {
            executor.setKeepAliveTime(props.getKeepAliveTime(), props.getUnit());
        }
        if (!Objects.equals(executor.allowsCoreThreadTimeOut(), props.isAllowCoreThreadTimeOut())) {
            executor.allowCoreThreadTimeOut(props.isAllowCoreThreadTimeOut());
        }
        // update queue
        updateQueueProps(executor, props);

        if (executor instanceof DtpExecutor) {
            doRefreshDtp((DtpExecutor) executor, props);
            return;
        }
        doRefreshCommon(executorWrapper, props);
    }

    private static void doRefreshCommon(ExecutorWrapper executorWrapper, DtpExecutorProps props) {

        if (StringUtils.isNotBlank(props.getThreadPoolAliasName())) {
            executorWrapper.setThreadPoolAliasName(props.getThreadPoolAliasName());
        }

        ExecutorAdapter<?> executor = executorWrapper.getExecutor();
        // update reject handler
        String currentRejectHandlerName = executor.getRejectHandlerName();
        if (!Objects.equals(currentRejectHandlerName, props.getRejectedHandlerType())) {
            val rejectHandler = RejectHandlerGetter.buildRejectedHandler(props.getRejectedHandlerType());
            executor.setRejectedExecutionHandler(rejectHandler);
        }

        // update notify related
        updateNotifyInfo(executorWrapper, props, dtpProperties.getPlatforms());
    }

    private static void doRefreshDtp(DtpExecutor executor, DtpExecutorProps props) {

        if (StringUtils.isNotBlank(props.getThreadPoolAliasName())) {
            executor.setThreadPoolAliasName(props.getThreadPoolAliasName());
        }
        // update reject handler
        if (!Objects.equals(executor.getRejectHandlerName(), props.getRejectedHandlerType())) {
            executor.setRejectedExecutionHandler(RejectHandlerGetter.getProxy(props.getRejectedHandlerType()));
            executor.setRejectHandlerName(props.getRejectedHandlerType());
        }
        executor.setWaitForTasksToCompleteOnShutdown(props.isWaitForTasksToCompleteOnShutdown());
        executor.setAwaitTerminationSeconds(props.getAwaitTerminationSeconds());
        executor.setPreStartAllCoreThreads(props.isPreStartAllCoreThreads());
        executor.setRunTimeout(props.getRunTimeout());
        executor.setQueueTimeout(props.getQueueTimeout());
        List<TaskWrapper> taskWrappers = TaskWrappers.getInstance().getByNames(props.getTaskWrapperNames());
        executor.setTaskWrappers(taskWrappers);

        // update notify related
        updateNotifyInfo(executor, props, dtpProperties.getPlatforms());
    }

    private static void doRefreshPoolSize(ExecutorAdapter<?> executor, DtpExecutorProps props) {
        if (props.getMaximumPoolSize() < executor.getMaximumPoolSize()) {
            if (!Objects.equals(executor.getCorePoolSize(), props.getCorePoolSize())) {
                executor.setCorePoolSize(props.getCorePoolSize());
            }
            if (!Objects.equals(executor.getMaximumPoolSize(), props.getMaximumPoolSize())) {
                executor.setMaximumPoolSize(props.getMaximumPoolSize());
            }
            return;
        }
        if (!Objects.equals(executor.getMaximumPoolSize(), props.getMaximumPoolSize())) {
            executor.setMaximumPoolSize(props.getMaximumPoolSize());
        }
        if (!Objects.equals(executor.getCorePoolSize(), props.getCorePoolSize())) {
            executor.setCorePoolSize(props.getCorePoolSize());
        }
    }

    private static void updateQueueProps(ExecutorAdapter<?> executor, DtpExecutorProps props) {

        val blockingQueue = executor.getQueue();
        if (blockingQueue instanceof MemorySafeLinkedBlockingQueue) {
            ((MemorySafeLinkedBlockingQueue<Runnable>) blockingQueue).setMaxFreeMemory(props.getMaxFreeMemory() * M_1);
        }
        if (!(blockingQueue instanceof VariableLinkedBlockingQueue)) {
            log.warn("DynamicTp refresh, the blockingqueue capacity cannot be reset, poolName: {}, queueType {}",
                    props.getThreadPoolName(), blockingQueue.getClass().getSimpleName());
            return;
        }

        int capacity = blockingQueue.size() + blockingQueue.remainingCapacity();
        if (!Objects.equals(capacity, props.getQueueCapacity())) {
            ((VariableLinkedBlockingQueue<Runnable>) blockingQueue).setCapacity(props.getQueueCapacity());
        }
    }

    @Autowired
    public void setDtpProperties(DtpProperties dtpProperties) {
        DtpRegistry.dtpProperties = dtpProperties;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public void run(ApplicationArguments args) {
        Set<String> remoteExecutors = Collections.emptySet();
        if (CollectionUtils.isNotEmpty(dtpProperties.getExecutors())) {
            remoteExecutors = dtpProperties.getExecutors().stream()
                    .map(DtpExecutorProps::getThreadPoolName)
                    .collect(Collectors.toSet());
        }

        val registeredExecutors = Sets.newHashSet(DTP_REGISTRY.keySet());
        registeredExecutors.addAll(COMMON_REGISTRY.keySet());
        val localExecutors = CollectionUtils.subtract(registeredExecutors, remoteExecutors);
        log.info("DtpRegistry has been initialized, remote executors: {}, local executors: {}",
                remoteExecutors, localExecutors);
    }
}
