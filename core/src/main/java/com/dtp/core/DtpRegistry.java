package com.dtp.core;

import com.dtp.common.entity.DtpMainProp;
import com.dtp.common.entity.NotifyPlatform;
import com.dtp.common.ex.DtpException;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.properties.ThreadPoolProperties;
import com.dtp.common.queue.MemorySafeLinkedBlockingQueue;
import com.dtp.common.queue.VariableLinkedBlockingQueue;
import com.dtp.common.util.StreamUtil;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.dtp.common.constant.DynamicTpConst.M_1;
import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;
import static com.dtp.common.entity.NotifyItem.mergeAllNotifyItems;
import static com.dtp.common.entity.NotifyItem.mergeSimpleNotifyItems;
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
        log.info("DynamicTp register commonExecutor, source: {}, name: {}",
                source, wrapper.getThreadPoolName());
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
            log.warn("DynamicTp refresh, cannot find specified dtpExecutor, name: {}.", x.getThreadPoolName());
        });
    }

    private static void refresh(ExecutorWrapper executorWrapper, ThreadPoolProperties properties) {

        if (properties.coreParamIsInValid()) {
            log.error("DynamicTp refresh, invalid parameters exist, properties: {}", properties);
            return;
        }
        DtpMainProp oldProp = ExecutorConverter.convert(executorWrapper);
        doRefresh(executorWrapper, properties);
        DtpMainProp newProp = ExecutorConverter.convert(executorWrapper);
        if (oldProp.equals(newProp)) {
            log.warn("DynamicTp refresh, main properties of [{}] have not changed.", executorWrapper.getThreadPoolName());
            return;
        }

        List<String> diffKeys = EQUATOR.getDiffFields(oldProp, newProp)
                .stream().map(FieldInfo::getFieldName).collect(toList());
        NoticeManager.doNoticeAsync(executorWrapper, oldProp, diffKeys);
        log.info("DynamicTp refresh, name: [{}], changed keys: {}, corePoolSize: [{}], maxPoolSize: [{}]," +
                        " queueType: [{}], queueCapacity: [{}], keepAliveTime: [{}], rejectedType: [{}]," +
                        " allowsCoreThreadTimeOut: [{}]", executorWrapper.getThreadPoolName(), diffKeys,
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

        if (!(executorWrapper.getExecutor() instanceof ThreadPoolExecutor)) {
            log.warn("DynamicTp refresh, cannot handle this executor, class: {}",
                    executorWrapper.getExecutor().getClass().getSimpleName());
            return;
        }

        ThreadPoolExecutor executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        doRefreshPoolSize(executor, properties);
        if (!Objects.equals(executor.getKeepAliveTime(properties.getUnit()), properties.getKeepAliveTime())) {
            executor.setKeepAliveTime(properties.getKeepAliveTime(), properties.getUnit());
        }
        if (!Objects.equals(executor.allowsCoreThreadTimeOut(), properties.isAllowCoreThreadTimeOut())) {
            executor.allowCoreThreadTimeOut(properties.isAllowCoreThreadTimeOut());
        }
        if (executor instanceof DtpExecutor) {
            doRefreshDtp((DtpExecutor) executor, properties);
            return;
        }
        doRefreshCommon(executorWrapper, properties);
    }

    private static void doRefreshCommon(ExecutorWrapper executorWrapper, ThreadPoolProperties properties) {

        if (StringUtils.isNotBlank(properties.getThreadPoolAliasName())) {
            executorWrapper.setThreadPoolAliasName(properties.getThreadPoolAliasName());
        }

        ThreadPoolExecutor executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        // update reject handler
        if (!Objects.equals(executor.getRejectedExecutionHandler().getClass().getSimpleName(),
                properties.getRejectedHandlerType())) {
            val rejectHandler = RejectHandlerGetter.buildRejectedHandler(properties.getRejectedHandlerType());
            executor.setRejectedExecutionHandler(rejectHandler);
        }

        updateQueueProp(properties, executor);

        // update notify related
        executorWrapper.setNotifyEnabled(properties.isNotifyEnabled());
        val allNotifyItems = mergeSimpleNotifyItems(properties.getNotifyItems());
        List<NotifyPlatform> notifyPlatforms = mergeNotifyPlatforms(executorWrapper.getThreadPoolName(), dtpProperties);
        NotifyHelper.refreshNotify(executorWrapper.getThreadPoolName(), notifyPlatforms,
                executorWrapper.getNotifyItems(), allNotifyItems);
        executorWrapper.setNotifyItems(allNotifyItems);
    }

    private static void doRefreshDtp(DtpExecutor executor, ThreadPoolProperties properties) {

        if (StringUtils.isNotBlank(properties.getThreadPoolAliasName())) {
            executor.setThreadPoolAliasName(properties.getThreadPoolAliasName());
        }
        // update reject handler
        if (!Objects.equals(executor.getRejectHandlerName(), properties.getRejectedHandlerType())) {
            executor.setRejectedExecutionHandler(RejectHandlerGetter.getProxy(properties.getRejectedHandlerType()));
            executor.setRejectHandlerName(properties.getRejectedHandlerType());
        }
        executor.setWaitForTasksToCompleteOnShutdown(properties.isWaitForTasksToCompleteOnShutdown());
        executor.setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds());
        executor.setPreStartAllCoreThreads(properties.isPreStartAllCoreThreads());
        executor.setRunTimeout(properties.getRunTimeout());
        executor.setQueueTimeout(properties.getQueueTimeout());
        updateQueueProp(properties, executor);
        List<TaskWrapper> taskWrappers = TaskWrappers.getInstance().getByNames(properties.getTaskWrapperNames());
        executor.setTaskWrappers(taskWrappers);

        // update notify related
        val allNotifyItems = mergeAllNotifyItems(properties.getNotifyItems());
        List<NotifyPlatform> notifyPlatforms = mergeNotifyPlatforms(executor.getThreadPoolName(), dtpProperties);
        NotifyHelper.refreshNotify(executor.getThreadPoolName(), notifyPlatforms,
                executor.getNotifyItems(), allNotifyItems);
        executor.setNotifyItems(allNotifyItems);
        executor.setNotifyEnabled(properties.isNotifyEnabled());
    }

    private static List<NotifyPlatform> mergeNotifyPlatforms(String threadPoolName, DtpProperties dtpProperties) {
        // 如果配置了线程池的通知平台，则使用线程池的通知平台，否则使用全局的通知平台
        List<NotifyPlatform> globalNotifyPlatform = dtpProperties.getPlatforms();
        for (ThreadPoolProperties properties : dtpProperties.getExecutors()) {
            if (Objects.equals(properties.getThreadPoolName(), threadPoolName)) {
                return mergeNotifyPlatforms(globalNotifyPlatform, properties.getPlatforms());
            }
        }
        return globalNotifyPlatform;
    }

    private static List<NotifyPlatform> mergeNotifyPlatforms(List<NotifyPlatform> globalPlatforms,
                                                             List<NotifyPlatform> platforms) {
        if (CollectionUtils.isEmpty(platforms) && CollectionUtils.isEmpty(globalPlatforms)) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(platforms)) {
            return globalPlatforms;
        }
        if (CollectionUtils.isEmpty(globalPlatforms)) {
            return platforms;
        }
        List<NotifyPlatform> curPlatforms = new ArrayList<>(platforms);
        // add global platforms if platforms isn't exists
        Map<String, NotifyPlatform> platformMap = StreamUtil.toMap(platforms, NotifyPlatform::getPlatform);
        for (NotifyPlatform globalPlatform : globalPlatforms) {
            if (!platformMap.containsKey(globalPlatform.getPlatform())) {
                curPlatforms.add(globalPlatform);
            }
        }
        return curPlatforms;
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

    private static void updateQueueProp(ThreadPoolProperties properties, ThreadPoolExecutor executor) {

        val blockingQueue = executor.getQueue();
        if (blockingQueue instanceof MemorySafeLinkedBlockingQueue) {
            ((MemorySafeLinkedBlockingQueue<Runnable>) blockingQueue).setMaxFreeMemory(properties.getMaxFreeMemory() * M_1);
        }
        if (!(blockingQueue instanceof VariableLinkedBlockingQueue)) {
            log.warn("DynamicTp refresh, the blockingqueue capacity cannot be reset, poolName: {}, queueType {}",
                    properties.getThreadPoolName(), blockingQueue.getClass().getSimpleName());
            return;
        }

        int capacity = blockingQueue.size() + blockingQueue.remainingCapacity();
        if (!Objects.equals(capacity, properties.getQueueCapacity())) {
            ((VariableLinkedBlockingQueue<Runnable>) blockingQueue).setCapacity(properties.getQueueCapacity());
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
                    .map(ThreadPoolProperties::getThreadPoolName)
                    .collect(Collectors.toSet());
        }

        val registeredExecutors = Sets.newHashSet(DTP_REGISTRY.keySet());
        registeredExecutors.addAll(COMMON_REGISTRY.keySet());
        val localExecutors = CollectionUtils.subtract(registeredExecutors, remoteExecutors);
        log.info("DtpRegistry initialization is complete, remote executors: {}, local executors: {}",
                remoteExecutors, localExecutors);
    }
}
