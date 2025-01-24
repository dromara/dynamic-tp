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

package org.dromara.dynamictp.adapter.common;

import com.github.dadiyang.equator.Equator;
import com.github.dadiyang.equator.FieldInfo;
import com.github.dadiyang.equator.GetterBaseEquator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.common.event.CustomContextRefreshedEvent;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.manager.EventBusManager;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.common.util.StreamUtil;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.converter.ExecutorConverter;
import org.dromara.dynamictp.core.notifier.manager.NoticeManager;
import org.dromara.dynamictp.core.support.adapter.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrappers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.stream.Collectors.toList;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;
import static org.dromara.dynamictp.core.notifier.manager.NotifyHelper.updateNotifyInfo;
import static org.dromara.dynamictp.core.support.DtpLifecycleSupport.shutdownGracefulAsync;

/**
 * AbstractDtpAdapter related
 *
 * @author yanhom
 * @author dragon-zhang
 * @since 1.0.6
 */
@Slf4j
public abstract class AbstractDtpAdapter implements DtpAdapter {

    private static final Equator EQUATOR = new GetterBaseEquator();

    protected final Map<String, ExecutorWrapper> executors = Maps.newHashMap();

    protected AbstractDtpAdapter() {
        EventBusManager.register(this);
    }

    @Subscribe
    public synchronized void onContextRefreshedEvent(CustomContextRefreshedEvent event) {
        try {
            DtpProperties dtpProperties = ContextManagerHelper.getBean(DtpProperties.class);
            initialize();
            afterInitialize();
            refresh(dtpProperties);
            log.info("DynamicTp adapter, {} init end, executors: {}", getTpPrefix(), executors);
        } catch (Throwable e) {
            log.error("DynamicTp adapter, {} init failed.", getTpPrefix(), e);
        }
    }

    protected void initialize() {
    }

    protected void afterInitialize() {
        getExecutorWrappers().forEach((k, v) -> AwareManager.register(v));
    }

    @Override
    public Map<String, ExecutorWrapper> getExecutorWrappers() {
        return executors;
    }

    /**
     * Get multi thread pool stats.
     *
     * @return thead pools stats
     */
    @Override
    public List<ThreadPoolStats> getMultiPoolStats() {
        val executorWrappers = getExecutorWrappers();
        if (MapUtils.isEmpty(executorWrappers)) {
            return Collections.emptyList();
        }

        List<ThreadPoolStats> threadPoolStats = Lists.newArrayList();
        executorWrappers.forEach((k, v) -> threadPoolStats.add(ExecutorConverter.toMetrics(v)));
        return threadPoolStats;
    }

    public void refresh(List<TpExecutorProps> propsList, List<NotifyPlatform> platforms) {
        val executorWrappers = getExecutorWrappers();
        if (CollectionUtils.isEmpty(propsList) || MapUtils.isEmpty(executorWrappers)) {
            return;
        }

        val tmpMap = StreamUtil.toMap(propsList, TpExecutorProps::getThreadPoolName);
        executorWrappers.forEach((k, v) -> refresh(v, platforms, tmpMap.get(k)));
    }

    public void refresh(ExecutorWrapper executorWrapper, List<NotifyPlatform> platforms, TpExecutorProps props) {

        if (Objects.isNull(props) || Objects.isNull(executorWrapper) || containsInvalidParams(props, log)) {
            return;
        }

        TpMainFields oldFields = getTpMainFields(executorWrapper, props);
        doRefresh(executorWrapper, platforms, props);
        TpMainFields newFields = getTpMainFields(executorWrapper, props);
        if (oldFields.equals(newFields)) {
            log.debug("DynamicTp adapter, main properties of [{}] have not changed.",
                    executorWrapper.getThreadPoolName());
            return;
        }

        List<FieldInfo> diffFields = EQUATOR.getDiffFields(oldFields, newFields);
        List<String> diffKeys = diffFields.stream().map(FieldInfo::getFieldName).collect(toList());
        NoticeManager.tryNoticeAsync(executorWrapper, oldFields, diffKeys);
        log.info("DynamicTp adapter, [{}] refreshed end, changed keys: {}, corePoolSize: [{}], "
                        + "maxPoolSize: [{}], keepAliveTime: [{}]",
                executorWrapper.getThreadPoolName(), diffKeys,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getCorePoolSize(), newFields.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getMaxPoolSize(), newFields.getMaxPoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getKeepAliveTime(), newFields.getKeepAliveTime()));
    }

    protected TpMainFields getTpMainFields(ExecutorWrapper executorWrapper, TpExecutorProps props) {
        return ExecutorConverter.toMainFields(executorWrapper);
    }

    /**
     * Get tp prefix.
     * @return tp prefix
     */
    protected abstract String getTpPrefix();

    protected void enhanceOriginExecutor(String tpName, ThreadPoolExecutor executor, String fieldName, Object targetObj) {
        ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(executor);
        ReflectionUtil.setFieldValue(fieldName, targetObj, proxy);
        putAndFinalize(tpName, executor, proxy);
    }

    protected void enhanceOriginExecutorByOfferingProxy(String tpName, ThreadPoolExecutorProxy proxy, String fieldName, Object targetObj) {
        ReflectionUtil.setFieldValue(fieldName, targetObj, proxy);
        executors.put(tpName, new ExecutorWrapper(tpName, proxy));
    }

    protected void putAndFinalize(String tpName, ExecutorService origin, Executor targetForWrapper) {
        executors.put(tpName, new ExecutorWrapper(tpName, targetForWrapper));
        shutdownOriginalExecutor(origin);
    }

    protected void shutdownOriginalExecutor(ExecutorService executor) {
        shutdownGracefulAsync(executor, getTpPrefix(), 5);
    }

    protected void doRefresh(ExecutorWrapper executorWrapper,
                             List<NotifyPlatform> platforms,
                             TpExecutorProps props) {

        val executor = executorWrapper.getExecutor();
        doRefreshPoolSize(executor, props);
        if (!Objects.equals(executor.getKeepAliveTime(props.getUnit()), props.getKeepAliveTime())) {
            executor.setKeepAliveTime(props.getKeepAliveTime(), props.getUnit());
        }
        if (StringUtils.isNotBlank(props.getThreadPoolAliasName())) {
            executorWrapper.setThreadPoolAliasName(props.getThreadPoolAliasName());
        }
        List<TaskWrapper> taskWrappers = TaskWrappers.getInstance().getByNames(props.getTaskWrapperNames());
        executorWrapper.setTaskWrappers(taskWrappers);

        // update notify items
        updateNotifyInfo(executorWrapper, props, platforms);
        // update aware related
        AwareManager.refresh(executorWrapper, props);
    }

    private void doRefreshPoolSize(ExecutorAdapter<?> executor, TpExecutorProps props) {
        if (props.getMaximumPoolSize() >= executor.getMaximumPoolSize()) {
            if (!Objects.equals(props.getMaximumPoolSize(), executor.getMaximumPoolSize())) {
                executor.setMaximumPoolSize(props.getMaximumPoolSize());
            }
            if (!Objects.equals(props.getCorePoolSize(), executor.getCorePoolSize())) {
                executor.setCorePoolSize(props.getCorePoolSize());
            }
            return;
        }

        if (!Objects.equals(props.getCorePoolSize(), executor.getCorePoolSize())) {
            executor.setCorePoolSize(props.getCorePoolSize());
        }
        if (!Objects.equals(props.getMaximumPoolSize(), executor.getMaximumPoolSize())) {
            executor.setMaximumPoolSize(props.getMaximumPoolSize());
        }
    }
}

