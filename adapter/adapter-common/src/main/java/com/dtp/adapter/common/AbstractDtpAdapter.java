package com.dtp.adapter.common;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.entity.NotifyPlatform;
import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.common.entity.TpExecutorProps;
import com.dtp.common.entity.TpMainFields;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.StreamUtil;
import com.dtp.core.convert.ExecutorConverter;
import com.dtp.core.convert.MetricsConverter;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.notify.manager.NoticeManager;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.support.ExecutorAdapter;
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
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;
import static com.dtp.core.notify.manager.NotifyHelper.updateNotifyInfo;
import static java.util.stream.Collectors.toList;

/**
 * AbstractDtpAdapter related
 *
 * @author yanhom
 * @author dragon-zhang
 * @since 1.0.6
 */
@Slf4j
public abstract class AbstractDtpAdapter implements DtpAdapter, GenericApplicationListener {

    private static final Equator EQUATOR = new GetterBaseEquator();

    protected final Map<String, ExecutorWrapper> executors = Maps.newHashMap();

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        Class<?> type = resolvableType.getRawClass();
        if (type != null) {
            return ApplicationReadyEvent.class.isAssignableFrom(type);
        }
        return false;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            try {
                DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
                initialize();
                refresh(dtpProperties);
            } catch (Exception e) {
                log.error("Init third party thread pool failed.", e);
            }
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

    public void refresh(String name, List<TpExecutorProps> propsList, List<NotifyPlatform> platforms) {
        val executorWrappers = getExecutorWrappers();
        if (CollectionUtils.isEmpty(propsList) || MapUtils.isEmpty(executorWrappers)) {
            return;
        }

        val tmpMap = StreamUtil.toMap(propsList, TpExecutorProps::getThreadPoolName);
        executorWrappers.forEach((k, v) -> refresh(name, v, platforms, tmpMap.get(k)));
    }

    public void refresh(String name,
                        ExecutorWrapper executorWrapper,
                        List<NotifyPlatform> platforms,
                        TpExecutorProps props) {

        if (Objects.isNull(props) || Objects.isNull(executorWrapper) || containsInvalidParams(props, log)) {
            return;
        }

        TpMainFields oldFields = getTpMainFields(executorWrapper, props);
        doRefresh(executorWrapper, platforms, props);
        TpMainFields newFields = getTpMainFields(executorWrapper, props);
        if (oldFields.equals(newFields)) {
            log.debug("DynamicTp adapter refresh, main properties of [{}] have not changed.",
                    executorWrapper.getThreadPoolName());
            return;
        }

        List<FieldInfo> diffFields = EQUATOR.getDiffFields(oldFields, newFields);
        List<String> diffKeys = diffFields.stream().map(FieldInfo::getFieldName).collect(toList());
        NoticeManager.doNoticeAsync(executorWrapper, oldFields, diffKeys);
        log.info("DynamicTp {} adapter, [{}] refreshed end, changed keys: {}, corePoolSize: [{}], "
                        + "maxPoolSize: [{}], keepAliveTime: [{}]",
                name, executorWrapper.getThreadPoolName(), diffKeys,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getCorePoolSize(), newFields.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getMaxPoolSize(), newFields.getMaxPoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldFields.getKeepAliveTime(), newFields.getKeepAliveTime()));
    }

    protected TpMainFields getTpMainFields(ExecutorWrapper executorWrapper, TpExecutorProps props) {
        return ExecutorConverter.convert(executorWrapper);
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

        // update notify items
        updateNotifyInfo(executorWrapper, props, platforms);
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
