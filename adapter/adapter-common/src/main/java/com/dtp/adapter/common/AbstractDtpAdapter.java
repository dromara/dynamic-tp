package com.dtp.adapter.common;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.dto.DtpMainProp;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.properties.SimpleTpProperties;
import com.dtp.common.util.StreamUtil;
import com.dtp.core.context.NoticeCtx;
import com.dtp.core.convert.ExecutorConverter;
import com.dtp.core.convert.MetricsConverter;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.notify.manager.NoticeManager;
import com.dtp.core.notify.manager.NotifyItemManager;
import com.github.dadiyang.equator.Equator;
import com.github.dadiyang.equator.FieldInfo;
import com.github.dadiyang.equator.GetterBaseEquator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;
import static com.dtp.common.dto.NotifyItem.mergeSimpleNotifyItems;
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

    protected void initialize() { }

    public void register(String poolName, ThreadPoolExecutor threadPoolExecutor) { }

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
        if (CollUtil.isEmpty(executorWrappers)) {
            return Collections.emptyList();
        }

        List<ThreadPoolStats> threadPoolStats = Lists.newArrayList();
        executorWrappers.forEach((k, v) -> threadPoolStats.add(MetricsConverter.convert(v)));
        return threadPoolStats;
    }

    public void initNotifyItems(String poolName, ExecutorWrapper executorWrapper) {
        AlarmManager.initAlarm(poolName, executorWrapper.getNotifyItems());
    }

    public void refresh(String name, List<SimpleTpProperties> properties, List<NotifyPlatform> platforms) {
        val executorWrappers = getExecutorWrappers();
        if (CollUtil.isEmpty(properties) || CollUtil.isEmpty(executorWrappers)) {
            return;
        }

        val tmpMap = StreamUtil.toMap(properties, SimpleTpProperties::getThreadPoolName);
        executorWrappers.forEach((k, v) -> refresh(name, v, platforms, tmpMap.get(k)));
    }

    public void refresh(String name,
                        ExecutorWrapper executorWrapper,
                        List<NotifyPlatform> platforms,
                        SimpleTpProperties properties) {

        if (Objects.isNull(properties) || Objects.isNull(executorWrapper) || containsInvalidParams(properties, log)) {
            return;
        }

        DtpMainProp oldProp = ExecutorConverter.convert(executorWrapper);
        doRefresh(executorWrapper, platforms, properties);
        DtpMainProp newProp = ExecutorConverter.convert(executorWrapper);
        if (oldProp.equals(newProp)) {
            log.warn("DynamicTp adapter refresh, main properties of [{}] have not changed.",
                    executorWrapper.getThreadPoolName());
            return;
        }

        List<FieldInfo> diffFields = EQUATOR.getDiffFields(oldProp, newProp);
        List<String> diffKeys = diffFields.stream().map(FieldInfo::getFieldName).collect(toList());
        log.info("DynamicTp {} adapter, [{}] refreshed end, changed keys: {}, corePoolSize: [{}], "
                        + "maxPoolSize: [{}], keepAliveTime: [{}]",
                name, executorWrapper.getThreadPoolName(), diffKeys,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getCorePoolSize(), newProp.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getMaxPoolSize(), newProp.getMaxPoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getKeepAliveTime(), newProp.getKeepAliveTime()));

        val notifyItem = NotifyItemManager.getNotifyItem(executorWrapper, NotifyItemEnum.CHANGE);
        NoticeCtx context = new NoticeCtx(executorWrapper, notifyItem, platforms, oldProp, diffKeys);
        NoticeManager.doNoticeAsync(context);
    }

    private void doRefresh(ExecutorWrapper executorWrapper,
                           List<NotifyPlatform> platforms,
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
        AlarmManager.refreshAlarm(executorWrapper.getThreadPoolName(), platforms,
                executorWrapper.getNotifyItems(), allNotifyItems);
        executorWrapper.setNotifyItems(allNotifyItems);
        executorWrapper.setNotifyEnabled(properties.isNotifyEnabled());
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
