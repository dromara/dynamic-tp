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

package org.dromara.dynamictp.core;

import com.github.dadiyang.equator.Equator;
import com.github.dadiyang.equator.FieldInfo;
import com.github.dadiyang.equator.GetterBaseEquator;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.common.ex.DtpException;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.queue.MemorySafeLinkedBlockingQueue;
import org.dromara.dynamictp.common.queue.VariableLinkedBlockingQueue;
import org.dromara.dynamictp.common.util.StreamUtil;
import org.dromara.dynamictp.core.converter.ExecutorConverter;
import org.dromara.dynamictp.core.notifier.manager.NoticeManager;
import org.dromara.dynamictp.core.notifier.manager.NotifyHelper;
import org.dromara.dynamictp.core.plugin.DtpExtension;
import org.dromara.dynamictp.core.reject.RejectHandlerGetter;
import org.dromara.dynamictp.core.support.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrappers;
import org.dromara.dynamictp.core.thread.DtpExecutor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.M_1;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;

/**
 * Core Registry, which keeps all registered Dynamic ThreadPoolExecutors.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpRegistry implements ApplicationRunner {

    /**
     * Maintain all automatically registered and manually registered Executors(DtpExecutors and JUC ThreadPoolExecutors).
     */
    private static final Map<String, ExecutorWrapper> EXECUTOR_REGISTRY = new ConcurrentHashMap<>();
    /**
     * equator for comparing two TpMainFields
     */
    private static final Equator EQUATOR = new GetterBaseEquator();

    /**
     * dtp properties
     */
    private static DtpProperties dtpProperties;

    public DtpRegistry(DtpProperties dtpProperties) {
        DtpRegistry.dtpProperties = dtpProperties;
    }

    /**
     * Get all Executor names.
     *
     * @return executor names
     */
    public static Set<String> listAllExecutorNames() {
        return Collections.unmodifiableSet(EXECUTOR_REGISTRY.keySet());
    }

    /**
     * Get all Executors.
     *
     * @return all Executors
     */
    public static Map<String, ExecutorWrapper> listAllExecutors() {
        return EXECUTOR_REGISTRY;
    }

    /**
     * Register a ThreadPoolExecutor.
     *
     * @param wrapper the newly created ThreadPoolExecutor wrapper instance
     * @param source  the source of the call to register method
     */
    public static void registerExecutor(ExecutorWrapper wrapper, String source) {
        log.info("DynamicTp register dtpExecutor, source: {}, executor: {}",
                source, ExecutorConverter.toMainFields(wrapper));
        EXECUTOR_REGISTRY.putIfAbsent(wrapper.getThreadPoolName(), wrapper);
    }

    /**
     * Get Dynamic ThreadPoolExecutor by thread pool name.
     *
     * @param name the name of dynamic thread pool
     * @return the managed DtpExecutor instance
     */
    public static DtpExecutor getDtpExecutor(final String name) {
        val executorWrapper = getExecutorWrapper(name);
        if (!executorWrapper.isDtpExecutor()) {
            log.error("The specified executor is not a DtpExecutor, name: {}", name);
            throw new DtpException("The specified executor is not a DtpExecutor, name: " + name);
        }
        return (DtpExecutor) executorWrapper.getExecutor();
    }

    /**
     * Get ThreadPoolExecutor by thread pool name.
     *
     * @param name the name of thread pool
     * @return the managed ExecutorWrapper instance
     */
    public static Executor getExecutor(final String name) {
        val executorWrapper = EXECUTOR_REGISTRY.get(name);
        if (Objects.isNull(executorWrapper)) {
            log.error("Cannot find a specified executor, name: {}", name);
            throw new DtpException("Cannot find a specified executor, name: " + name);
        }
        return executorWrapper.getExecutor();
    }

    /**
     * Get ExecutorWrapper by thread pool name.
     *
     * @param name the name of thread pool
     * @return the managed ExecutorWrapper instance
     */
    public static ExecutorWrapper getExecutorWrapper(final String name) {
        ExecutorWrapper executor = EXECUTOR_REGISTRY.get(name);
        if (Objects.isNull(executor)) {
            log.error("Cannot find a specified executorWrapper, name: {}", name);
            throw new DtpException("Cannot find a specified executorWrapper, name: " + name);
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
            ExecutorWrapper executorWrapper = EXECUTOR_REGISTRY.get(x.getThreadPoolName());
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
        TpMainFields oldFields = ExecutorConverter.toMainFields(executorWrapper);
        doRefresh(executorWrapper, props);
        TpMainFields newFields = ExecutorConverter.toMainFields(executorWrapper);
        if (oldFields.equals(newFields)) {
            log.debug("DynamicTp refresh, main properties of [{}] have not changed.",
                    executorWrapper.getThreadPoolName());
            return;
        }
        // Get the changed keys
        List<FieldInfo> diffFields = EQUATOR.getDiffFields(oldFields, newFields);
        List<String> diffKeys = StreamUtil.fetchProperty(diffFields, FieldInfo::getFieldName);
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
            doRefreshDtp(executorWrapper, props);
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
        String currentRejectHandlerType = executor.getRejectHandlerType();
        if (!Objects.equals(currentRejectHandlerType, props.getRejectedHandlerType())) {
            val rejectHandler = RejectHandlerGetter.buildRejectedHandler(props.getRejectedHandlerType());
            executor.setRejectedExecutionHandler(rejectHandler);
        }

        // update notify related
        NotifyHelper.updateNotifyInfo(executorWrapper, props, dtpProperties.getPlatforms());
    }

    private static void doRefreshDtp(ExecutorWrapper executorWrapper, DtpExecutorProps props) {

        DtpExecutor executor = (DtpExecutor) executorWrapper.getExecutor();
        if (StringUtils.isNotBlank(props.getThreadPoolAliasName())) {
            executor.setThreadPoolAliasName(props.getThreadPoolAliasName());
        }
        // update reject handler
        executor.setRejectEnhanced(props.isRejectEnhanced());
        if (!Objects.equals(executor.getRejectHandlerType(), props.getRejectedHandlerType())) {
            executor.setRejectHandler(RejectHandlerGetter.buildRejectedHandler(props.getRejectedHandlerType()));
        }
        executor.setWaitForTasksToCompleteOnShutdown(props.isWaitForTasksToCompleteOnShutdown());
        executor.setAwaitTerminationSeconds(props.getAwaitTerminationSeconds());
        executor.setPreStartAllCoreThreads(props.isPreStartAllCoreThreads());
        executor.setRunTimeout(props.getRunTimeout());
        executor.setQueueTimeout(props.getQueueTimeout());
        List<TaskWrapper> taskWrappers = TaskWrappers.getInstance().getByNames(props.getTaskWrapperNames());
        executor.setTaskWrappers(taskWrappers);

        // update notify related
        NotifyHelper.updateNotifyInfo(executor, props, dtpProperties.getPlatforms());
        updateWrapper(executorWrapper, executor);
    }

    private static void updateWrapper(ExecutorWrapper executorWrapper, DtpExecutor executor) {
        executorWrapper.setThreadPoolAliasName(executor.getThreadPoolAliasName());
        executorWrapper.setNotifyItems(executor.getNotifyItems());
        executorWrapper.setPlatformIds(executor.getPlatformIds());
        executorWrapper.setNotifyEnabled(executor.isNotifyEnabled());
    }

    /**
     * Why does it seem so complicated to handle this?
     * Although JDK9 solves this bug, we need to ensure that corePoolSize is less than or equal to maximumPoolSize,
     * otherwise an IllegalArgumentException will be thrown
     *
     * @param executor the executor
     * @param props    properties
     * @see <a href="https://bugs.openjdk.org/browse/JDK-7153400">JDK-7153400</a>
     */
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
        if (blockingQueue instanceof VariableLinkedBlockingQueue) {
            int capacity = blockingQueue.size() + blockingQueue.remainingCapacity();
            if (!Objects.equals(capacity, props.getQueueCapacity())) {
                ((VariableLinkedBlockingQueue<Runnable>) blockingQueue).setCapacity(props.getQueueCapacity());
                executor.onRefreshQueueCapacity(props.getQueueCapacity());
            }
            return;
        }
        log.warn("DynamicTp refresh, the blockingqueue capacity cannot be reset, poolName: {}, queueType {}",
                props.getThreadPoolName(), blockingQueue.getClass().getSimpleName());
    }

    @Override
    public void run(ApplicationArguments args) {
        Set<String> remoteExecutors = Collections.emptySet();
        if (CollectionUtils.isNotEmpty(dtpProperties.getExecutors())) {
            remoteExecutors = dtpProperties.getExecutors().stream()
                    .map(DtpExecutorProps::getThreadPoolName)
                    .collect(Collectors.toSet());
        }
        val registeredExecutors = Sets.newHashSet(EXECUTOR_REGISTRY.keySet());
        val localExecutors = CollectionUtils.subtract(registeredExecutors, remoteExecutors);
        log.info("DtpRegistry has been initialized, remote executors: {}, local executors: {}",
                remoteExecutors, localExecutors);
    }
}
