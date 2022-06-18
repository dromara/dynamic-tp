package com.dtp.adapter.common;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.dto.DtpMainProp;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.context.DtpContext;
import com.dtp.core.context.DtpContextHolder;
import com.dtp.core.convert.ExecutorConverter;
import com.dtp.core.handler.NotifierHandler;
import com.dtp.core.notify.AlarmCounter;
import com.dtp.core.notify.AlarmLimiter;
import com.dtp.core.notify.NotifyHelper;
import com.github.dadiyang.equator.Equator;
import com.github.dadiyang.equator.FieldInfo;
import com.github.dadiyang.equator.GetterBaseEquator;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dtp.common.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;
import static com.dtp.common.dto.NotifyItem.mergeSimpleNotifyItems;
import static java.util.stream.Collectors.toList;

/**
 * AbstractDtpHandler related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public abstract class AbstractDtpHandler implements DtpHandler, ApplicationListener<ApplicationStartedEvent> {

    private static final Equator EQUATOR = new GetterBaseEquator();

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        try {
            DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
            initialize();
            refresh(dtpProperties);
        } catch (Exception e) {
            log.error("Init third party thread pool failed.", e);
        }
    }

    protected void initialize() {}

    public void register(String poolName, ThreadPoolExecutor threadPoolExecutor) {}

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
        executorWrappers.forEach((k, v) -> {
            val e = (ThreadPoolExecutor) v.getExecutor();
            val stats = ThreadPoolStats.builder()
                    .corePoolSize(e.getCorePoolSize())
                    .maximumPoolSize(e.getMaximumPoolSize())
                    .queueType(e.getQueue().getClass().getSimpleName())
                    .queueCapacity(e.getQueue().size() + e.getQueue().remainingCapacity())
                    .queueSize(e.getQueue().size())
                    .queueRemainingCapacity(e.getQueue().remainingCapacity())
                    .activeCount(e.getActiveCount())
                    .taskCount(e.getTaskCount())
                    .completedTaskCount(e.getCompletedTaskCount())
                    .largestPoolSize(e.getLargestPoolSize())
                    .poolSize(e.getPoolSize())
                    .waitTaskCount(e.getQueue().size())
                    .poolName(k)
                    .build();
            threadPoolStats.add(stats);
        });
        return threadPoolStats;
    }

    public void initNotifyItems(String poolName, ExecutorWrapper executorWrapper) {
        executorWrapper.getNotifyItems().forEach(x -> {
            AlarmLimiter.initAlarmLimiter(poolName, x);
            AlarmCounter.init(poolName, x.getType());
        });
    }

    public void refresh(String name,
                        ExecutorWrapper executorWrapper,
                        List<NotifyPlatform> platforms,
                        SimpleTpProperties properties) {

        if (Objects.isNull(properties)) {
            return;
        }
        checkParams(properties);
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
        log.info("DynamicTp {} adapter, [{}] refreshed end, changed keys: {}, corePoolSize: [{}], " +
                        "maxPoolSize: [{}], keepAliveTime: [{}]",
                name, executorWrapper.getThreadPoolName(), diffKeys,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getCorePoolSize(), properties.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getMaxPoolSize(), properties.getMaximumPoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getKeepAliveTime(), properties.getKeepAliveTime()));

        val notifyItem = NotifyHelper.getNotifyItem(executorWrapper, NotifyTypeEnum.CHANGE);
        boolean ifNotice = CollUtil.isNotEmpty(platforms) && Objects.nonNull(notifyItem) && notifyItem.isEnabled();
        if (!ifNotice) {
            return;
        }
        DtpContext context = DtpContext.builder()
                .executorWrapper(executorWrapper)
                .platforms(platforms)
                .notifyItem(notifyItem)
                .build();
        DtpContextHolder.set(context);
        NotifierHandler.getInstance().sendNoticeAsync(oldProp, diffKeys);
    }

    private void doRefresh(ExecutorWrapper executorWrapper,
                           List<NotifyPlatform> platforms,
                           SimpleTpProperties properties) {

        val executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        if (!Objects.equals(executor.getCorePoolSize(), properties.getCorePoolSize())) {
            executor.setCorePoolSize(properties.getCorePoolSize());
        }

        if (!Objects.equals(executor.getKeepAliveTime(properties.getUnit()), properties.getKeepAliveTime())) {
            executor.setKeepAliveTime(properties.getKeepAliveTime(), properties.getUnit());
        }

        if (!Objects.equals(executor.getMaximumPoolSize(), properties.getMaximumPoolSize())) {
            executor.setMaximumPoolSize(properties.getMaximumPoolSize());
        }

        // update notify items
        properties.setNotifyItems(mergeSimpleNotifyItems(properties.getNotifyItems()));
        val items = NotifyHelper.fillNotifyItems(properties.getNotifyItems(), platforms);
        NotifyHelper.initAlarm(executorWrapper.getThreadPoolName(), executorWrapper.getNotifyItems(), items);
        executorWrapper.setNotifyItems(items);
    }
}
