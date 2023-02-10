package com.dtp.core;

import com.dtp.common.entity.DtpMainProp;
import com.dtp.common.ex.DtpException;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.properties.ThreadPoolProperties;
import com.dtp.common.queue.MemorySafeLinkedBlockingQueue;
import com.dtp.common.queue.VariableLinkedBlockingQueue;
import com.dtp.core.convert.ExecutorConverter;
import com.dtp.core.notify.manager.NoticeManager;
import com.dtp.core.notify.manager.NotifyHelper;
import com.dtp.core.reject.RejectHandlerGetter;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.support.wrapper.TaskWrapper;
import com.dtp.core.support.wrapper.TaskWrappers;
import com.dtp.core.thread.DtpExecutor;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.dtp.common.constant.DynamicTpConst.M_1;
import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;
import static com.dtp.common.entity.NotifyItem.mergeAllNotifyItems;
import static com.dtp.common.em.QueueTypeEnum.MEMORY_SAFE_LINKED_BLOCKING_QUEUE;
import static com.dtp.common.em.QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE;
import static com.dtp.core.support.ExecutorType.EAGER;
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
     * @param source the source of the call to register method
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
     * @param source the source of the call to register method
     */
    public static void registerCommon(ExecutorWrapper wrapper, String source) {
        log.info("DynamicTp register commonExecutor, source: {}, name: {}", source, wrapper.getThreadPoolName());
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
     * @param properties the main properties that maintain by config center
     */
    public static void refresh(DtpProperties properties) {
        if (Objects.isNull(properties) || CollectionUtils.isEmpty(properties.getExecutors())) {
            log.warn("DynamicTp refresh, empty threadPoolProperties.");
            return;
        }
        properties.getExecutors().forEach(x -> {
            if (StringUtils.isBlank(x.getThreadPoolName())) {
                log.warn("DynamicTp refresh, threadPoolName must not be empty.");
                return;
            }
            // First look in the DTP_REGISTRY
            val dtpExecutor = DTP_REGISTRY.get(x.getThreadPoolName());
            if (Objects.nonNull(dtpExecutor)) {
                refresh(dtpExecutor, x);
                return;
            }
            // And then look it up in the COMMON_REGISTRY
            val executorWrapper = COMMON_REGISTRY.get(x.getThreadPoolName());
            if (Objects.nonNull(executorWrapper)) {
                refreshCommon(executorWrapper, x);
                return;
            }
            log.warn("DynamicTp refresh, cannot find specified dtpExecutor, name: {}.", x.getThreadPoolName());
        });
    }

    /**
     * refresh common executor
     */
    private static void refreshCommon(ExecutorWrapper executorWrapper, ThreadPoolProperties properties) {

        if (properties.coreParamIsInValid()) {
            log.error("DynamicTp refreshCommon, invalid parameters exist, properties: {}", properties);
            return;
        }

        DtpMainProp oldProp = ExecutorConverter.convert(executorWrapper);
        doRefresh(executorWrapper, properties);
        DtpMainProp newProp = ExecutorConverter.convert(executorWrapper);
        if (oldProp.equals(newProp)) {
            log.warn("DynamicTp refreshCommon, main properties of [{}] have not changed.", executorWrapper.getThreadPoolName());
            return;
        }

        List<String> diffKeys = EQUATOR.getDiffFields(oldProp, newProp).stream().map(FieldInfo::getFieldName).collect(toList());
        NoticeManager.doNoticeAsync(executorWrapper, oldProp, diffKeys);
        printRefreshLog(oldProp, newProp, diffKeys, executorWrapper.getThreadPoolName());
    }

    private static void refresh(DtpExecutor executor, ThreadPoolProperties properties) {

        if (properties.coreParamIsInValid()) {
            log.error("DynamicTp refresh, invalid parameters exist, properties: {}", properties);
            return;
        }

        DtpMainProp oldProp = ExecutorConverter.convert(executor);
        doRefresh(executor, properties);
        DtpMainProp newProp = ExecutorConverter.convert(executor);
        if (oldProp.equals(newProp)) {
            log.warn("DynamicTp refresh, main properties of [{}] have not changed.", executor.getThreadPoolName());
            return;
        }

        List<FieldInfo> diffFields = EQUATOR.getDiffFields(oldProp, newProp);
        List<String> diffKeys = diffFields.stream().map(FieldInfo::getFieldName).collect(toList());
        ExecutorWrapper executorWrapper = new ExecutorWrapper(executor);
        NoticeManager.doNoticeAsync(executorWrapper, oldProp, diffKeys);
        printRefreshLog(oldProp, newProp, diffKeys, executor.getThreadPoolName());
    }

    private static void printRefreshLog(DtpMainProp oldProp,
                                        DtpMainProp newProp,
                                        List<String> diffKeys,
                                        String threadPoolName) {
        log.info("DynamicTp refresh, name: [{}], changed keys: {}, corePoolSize: [{}], maxPoolSize: [{}], queueType: [{}], " +
                        "queueCapacity: [{}], keepAliveTime: [{}], rejectedType: [{}], allowsCoreThreadTimeOut: [{}]",
                threadPoolName,
                diffKeys,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getCorePoolSize(), newProp.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getMaxPoolSize(), newProp.getMaxPoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getQueueType(), newProp.getQueueType()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getQueueCapacity(), newProp.getQueueCapacity()),
                String.format("%ss => %ss", oldProp.getKeepAliveTime(), newProp.getKeepAliveTime()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getRejectType(), newProp.getRejectType()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.isAllowCoreThreadTimeOut(),
                        newProp.isAllowCoreThreadTimeOut()));
    }

    private static void doRefresh(ExecutorWrapper executorWrapper, ThreadPoolProperties properties) {
        // Handles the java.util.concurrent.ThreadPoolExecutor type
        if (executorWrapper.getExecutor() instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
            doRefreshPoolSize(executor, properties);
            if (!Objects.equals(executor.getKeepAliveTime(properties.getUnit()), properties.getKeepAliveTime())) {
                executor.setKeepAliveTime(properties.getKeepAliveTime(), properties.getUnit());
            }

            if (!Objects.equals(executor.allowsCoreThreadTimeOut(), properties.isAllowCoreThreadTimeOut())) {
                executor.allowCoreThreadTimeOut(properties.isAllowCoreThreadTimeOut());
            }

            // update reject handler
            if (!Objects.equals(executor.getRejectedExecutionHandler().getClass().getSimpleName(), properties.getRejectedHandlerType())) {
                executor.setRejectedExecutionHandler(RejectHandlerGetter.buildRejectedHandler(properties.getRejectedHandlerType()));
            }
        }
        // handle other type ...
    }

    private static void doRefresh(DtpExecutor dtpExecutor, ThreadPoolProperties properties) {
        doRefreshPoolSize(dtpExecutor, properties);
        if (!Objects.equals(dtpExecutor.getKeepAliveTime(properties.getUnit()), properties.getKeepAliveTime())) {
            dtpExecutor.setKeepAliveTime(properties.getKeepAliveTime(), properties.getUnit());
        }

        if (!Objects.equals(dtpExecutor.allowsCoreThreadTimeOut(), properties.isAllowCoreThreadTimeOut())) {
            dtpExecutor.allowCoreThreadTimeOut(properties.isAllowCoreThreadTimeOut());
        }

        // update reject handler
        if (!Objects.equals(dtpExecutor.getRejectHandlerName(), properties.getRejectedHandlerType())) {
            dtpExecutor.setRejectedExecutionHandler(RejectHandlerGetter.getProxy(properties.getRejectedHandlerType()));
            dtpExecutor.setRejectHandlerName(properties.getRejectedHandlerType());
        }

        // update Alias Name
        if (!Objects.equals(dtpExecutor.getThreadPoolAliasName(), properties.getThreadPoolAliasName())) {
            dtpExecutor.setThreadPoolAliasName(properties.getThreadPoolAliasName());
        }

        updateQueueProp(properties, dtpExecutor);
        dtpExecutor.setWaitForTasksToCompleteOnShutdown(properties.isWaitForTasksToCompleteOnShutdown());
        dtpExecutor.setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds());
        dtpExecutor.setPreStartAllCoreThreads(properties.isPreStartAllCoreThreads());
        dtpExecutor.setRunTimeout(properties.getRunTimeout());
        dtpExecutor.setQueueTimeout(properties.getQueueTimeout());

        List<TaskWrapper> taskWrappers = TaskWrappers.getInstance().getByNames(properties.getTaskWrapperNames());
        dtpExecutor.setTaskWrappers(taskWrappers);

        // update notify items
        val allNotifyItems = mergeAllNotifyItems(properties.getNotifyItems());
        NotifyHelper.refreshNotify(dtpExecutor.getThreadPoolName(), dtpProperties.getPlatforms(),
                dtpExecutor.getNotifyItems(), allNotifyItems);
        dtpExecutor.setNotifyItems(allNotifyItems);
        dtpExecutor.setNotifyEnabled(properties.isNotifyEnabled());
    }

    @Autowired
    public void setDtpProperties(DtpProperties dtpProperties) {
        DtpRegistry.dtpProperties = dtpProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        Set<String> remoteExecutors = Collections.emptySet();
        if (CollectionUtils.isNotEmpty(dtpProperties.getExecutors())) {
            remoteExecutors = dtpProperties.getExecutors().stream()
                    .map(ThreadPoolProperties::getThreadPoolName)
                    .collect(Collectors.toSet());
        }

        val registeredDtpExecutors = Sets.newHashSet(DTP_REGISTRY.keySet());
        val localDtpExecutors = CollectionUtils.subtract(registeredDtpExecutors, remoteExecutors);
        log.info("DtpRegistry initialization is complete, remote dtpExecutors: {}, local dtpExecutors: {}, local commonExecutors: {}",
                remoteExecutors, localDtpExecutors, COMMON_REGISTRY.keySet());
    }

    private static void doRefreshPoolSize(ThreadPoolExecutor dtpExecutor, ThreadPoolProperties properties) {
        if (properties.getMaximumPoolSize() < dtpExecutor.getMaximumPoolSize()) {
            if (!Objects.equals(dtpExecutor.getCorePoolSize(), properties.getCorePoolSize())) {
                dtpExecutor.setCorePoolSize(properties.getCorePoolSize());
            }
            if (!Objects.equals(dtpExecutor.getMaximumPoolSize(), properties.getMaximumPoolSize())) {
                dtpExecutor.setMaximumPoolSize(properties.getMaximumPoolSize());
            }
            return;
        }
        if (!Objects.equals(dtpExecutor.getMaximumPoolSize(), properties.getMaximumPoolSize())) {
            dtpExecutor.setMaximumPoolSize(properties.getMaximumPoolSize());
        }
        if (!Objects.equals(dtpExecutor.getCorePoolSize(), properties.getCorePoolSize())) {
            dtpExecutor.setCorePoolSize(properties.getCorePoolSize());
        }
    }

    private static void updateQueueProp(ThreadPoolProperties properties, DtpExecutor dtpExecutor) {
        // update work queue
        if (!canModifyQueueProp(properties)) {
            return;
        }
        val blockingQueue = dtpExecutor.getQueue();
        if (!Objects.equals(dtpExecutor.getQueueCapacity(), properties.getQueueCapacity())) {
            if (blockingQueue instanceof VariableLinkedBlockingQueue) {
                ((VariableLinkedBlockingQueue<Runnable>) blockingQueue).setCapacity(properties.getQueueCapacity());
            } else {
                log.error("DynamicTp refresh, the blockingqueue capacity cannot be reset, dtpName: {}, queueType {}",
                        dtpExecutor.getThreadPoolName(), dtpExecutor.getQueueName());
            }
        }

        if (blockingQueue instanceof MemorySafeLinkedBlockingQueue) {
            ((MemorySafeLinkedBlockingQueue<Runnable>) blockingQueue).setMaxFreeMemory(properties.getMaxFreeMemory() * M_1);
        }
    }

    private static boolean canModifyQueueProp(ThreadPoolProperties properties) {
        return Objects.equals(properties.getQueueType(), VARIABLE_LINKED_BLOCKING_QUEUE.getName())
                || Objects.equals(properties.getQueueType(), MEMORY_SAFE_LINKED_BLOCKING_QUEUE.getName())
                || Objects.equals(properties.getExecutorType(), EAGER.getName());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
