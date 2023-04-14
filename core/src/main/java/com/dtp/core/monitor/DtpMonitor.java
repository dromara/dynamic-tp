package com.dtp.core.monitor;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.common.event.AlarmCheckEvent;
import com.dtp.common.event.CollectEvent;
import com.dtp.common.properties.DtpProperties;
import com.dtp.core.DtpRegistry;
import com.dtp.core.converter.MetricsConverter;
import com.dtp.core.handler.CollectorHandler;
import com.dtp.core.notifier.manager.AlarmManager;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.dtp.common.constant.DynamicTpConst.SCHEDULE_NOTIFY_ITEMS;

/**
 * DtpMonitor related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpMonitor implements InitializingBean, Ordered {

    private static final ScheduledExecutorService MONITOR_EXECUTOR = new ScheduledThreadPoolExecutor(
            1, new NamedThreadFactory("dtp-monitor", true));

    @Resource
    private DtpProperties dtpProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        MONITOR_EXECUTOR.scheduleWithFixedDelay(this::run,
                0, dtpProperties.getMonitorInterval(), TimeUnit.SECONDS);
    }

    private void run() {
        List<String> dtpNames = DtpRegistry.listAllDtpNames();
        List<String> commonNames = DtpRegistry.listAllCommonNames();
        checkAlarm(dtpNames, commonNames);
        collect(dtpNames, commonNames);
    }

    private void collect(List<String> dtpNames, List<String> commonNames) {
        if (!dtpProperties.isEnabledCollect()) {
            return;
        }

        dtpNames.forEach(x -> {
            DtpExecutor executor = DtpRegistry.getDtpExecutor(x);
            doCollect(MetricsConverter.convert(executor));
        });
        commonNames.forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getCommonExecutor(x);
            doCollect(MetricsConverter.convert(wrapper));
        });
        publishCollectEvent();
    }

    private void checkAlarm(List<String> dtpNames, List<String> commonNames) {
        dtpNames.forEach(x -> {
            DtpExecutor executor = DtpRegistry.getDtpExecutor(x);
            AlarmManager.doAlarmAsync(executor, SCHEDULE_NOTIFY_ITEMS);
        });
        commonNames.forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getCommonExecutor(x);
            AlarmManager.doAlarmAsync(wrapper, SCHEDULE_NOTIFY_ITEMS);
        });
        publishAlarmCheckEvent();
    }

    private void doCollect(ThreadPoolStats threadPoolStats) {
        try {
            CollectorHandler.getInstance().collect(threadPoolStats, dtpProperties.getCollectorTypes());
        } catch (Exception e) {
            log.error("DynamicTp monitor, metrics collect error.", e);
        }
    }

    private void publishCollectEvent() {
        CollectEvent event = new CollectEvent(this, dtpProperties);
        ApplicationContextHolder.publishEvent(event);
    }

    private void publishAlarmCheckEvent() {
        AlarmCheckEvent event = new AlarmCheckEvent(this, dtpProperties);
        ApplicationContextHolder.publishEvent(event);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
