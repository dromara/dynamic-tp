package com.dtp.adapter.common;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.entity.DtpMainProp;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.common.entity.NotifyPlatform;
import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.properties.SimpleTpProperties;
import com.dtp.common.util.StreamUtil;
import com.dtp.core.convert.ExecutorConverter;
import com.dtp.core.convert.MetricsConverter;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.notify.manager.NoticeManager;
import com.dtp.core.notify.manager.NotifyHelper;
import com.github.dadiyang.equator.Equator;
import com.github.dadiyang.equator.FieldInfo;
import com.github.dadiyang.equator.GetterBaseEquator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;
import static com.dtp.common.entity.NotifyItem.mergeSimpleNotifyItems;
import static java.util.stream.Collectors.toList;

/**
 * AbstractDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public abstract class AbstractDtpAdapter implements DtpAdapter, ApplicationListener<ApplicationReadyEvent> {

    private static final Equator EQUATOR = new GetterBaseEquator();

    protected final Map<String, ExecutorWrapper> executors = Maps.newHashMap();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
            initialize();
            refresh(dtpProperties);
        } catch (Exception e) {
            log.error("Init third party thread pool failed.", e);
        }
    }

    protected void initialize() {
    }

    public void register(String poolName, ThreadPoolExecutor threadPoolExecutor) {
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
        executorWrappers.forEach((k, v) -> threadPoolStats.add(MetricsConverter.convert(v)));
        return threadPoolStats;
    }

    public void initNotifyItems(String poolName, ExecutorWrapper executorWrapper) {
        AlarmManager.initAlarm(poolName, executorWrapper.getNotifyItems());
    }

    public void refresh(String name, List<SimpleTpProperties> properties, List<NotifyPlatform> globalPlatforms) {
        val executorWrappers = getExecutorWrappers();
        if (CollectionUtils.isEmpty(properties) || MapUtils.isEmpty(executorWrappers)) {
            return;
        }

        val tmpMap = StreamUtil.toMap(properties, SimpleTpProperties::getThreadPoolName);
        executorWrappers.forEach((k, v) -> refresh(name, v, globalPlatforms, tmpMap.get(k)));
    }


    public void refresh(String name,
                        ExecutorWrapper executorWrapper,
                        List<NotifyPlatform> globalPlatforms,
                        SimpleTpProperties properties) {

        if (Objects.isNull(properties) || Objects.isNull(executorWrapper) || containsInvalidParams(properties, log)) {
            return;
        }

        DtpMainProp oldProp = ExecutorConverter.convert(executorWrapper);
        doRefresh(executorWrapper, globalPlatforms, properties);
        DtpMainProp newProp = ExecutorConverter.convert(executorWrapper);
        if (oldProp.equals(newProp)) {
            log.warn("DynamicTp adapter refresh, main properties of [{}] have not changed.",
                    executorWrapper.getThreadPoolName());
            return;
        }

        List<FieldInfo> diffFields = EQUATOR.getDiffFields(oldProp, newProp);
        List<String> diffKeys = diffFields.stream().map(FieldInfo::getFieldName).collect(toList());
        NoticeManager.doNoticeAsync(executorWrapper, oldProp, diffKeys);
        log.info("DynamicTp {} adapter, [{}] refreshed end, changed keys: {}, corePoolSize: [{}], "
                        + "maxPoolSize: [{}], keepAliveTime: [{}]",
                name, executorWrapper.getThreadPoolName(), diffKeys,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getCorePoolSize(), newProp.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getMaxPoolSize(), newProp.getMaxPoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getKeepAliveTime(), newProp.getKeepAliveTime()));
    }

    private void doRefresh(ExecutorWrapper executorWrapper,
                           List<NotifyPlatform> globalPlatforms,
                           SimpleTpProperties properties) {

        val executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        doRefreshPoolSize(executor, properties);
        if (!Objects.equals(executor.getKeepAliveTime(properties.getUnit()), properties.getKeepAliveTime())) {
            executor.setKeepAliveTime(properties.getKeepAliveTime(), properties.getUnit());
        }
        if (StringUtils.isNotBlank(properties.getThreadPoolAliasName())) {
            executorWrapper.setThreadPoolAliasName(properties.getThreadPoolAliasName());
        }

        // update notify items
        val allNotifyItems = mergeSimpleNotifyItems(properties.getNotifyItems());
        // update notify platforms
        val allNotifyPlatforms = mergeNotifyPlatforms(globalPlatforms, properties.getPlatforms());
        NotifyHelper.refreshNotify(executorWrapper.getThreadPoolName(), allNotifyPlatforms,
                executorWrapper.getNotifyItems(), allNotifyItems);
        executorWrapper.setNotifyItems(allNotifyItems);
        executorWrapper.setNotifyEnabled(properties.isNotifyEnabled());
    }

    private static List<NotifyPlatform> mergeNotifyPlatforms(List<NotifyPlatform> globalPlatforms,
                                                             List<NotifyPlatform> platforms) {
        if (CollectionUtils.isEmpty(platforms) &&
                CollectionUtils.isEmpty(globalPlatforms)) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(platforms)) {
            return new ArrayList<>(globalPlatforms);
        }
        if (CollectionUtils.isEmpty(globalPlatforms)) {
            return new ArrayList<>(platforms);
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

    private void doRefreshPoolSize(ThreadPoolExecutor executor, SimpleTpProperties properties) {
        if (properties.getMaximumPoolSize() >= executor.getMaximumPoolSize()) {
            if (!Objects.equals(properties.getMaximumPoolSize(), executor.getMaximumPoolSize())) {
                executor.setMaximumPoolSize(properties.getMaximumPoolSize());
            }
            if (!Objects.equals(properties.getCorePoolSize(), executor.getCorePoolSize())) {
                executor.setCorePoolSize(properties.getCorePoolSize());
            }
            return;
        }

        if (!Objects.equals(properties.getCorePoolSize(), executor.getCorePoolSize())) {
            executor.setCorePoolSize(properties.getCorePoolSize());
        }
        if (!Objects.equals(properties.getMaximumPoolSize(), executor.getMaximumPoolSize())) {
            executor.setMaximumPoolSize(properties.getMaximumPoolSize());
        }
    }
}
