package com.dtp.core.monitor;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.common.event.AlarmCheckEvent;
import com.dtp.common.event.CollectEvent;
import com.dtp.core.DtpRegistry;
import com.dtp.core.convert.MetricsConverter;
import com.dtp.core.handler.CollectorHandler;
import com.dtp.core.notify.alarm.AlarmManager;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.core.Ordered;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.dtp.common.constant.DynamicTpConst.SCHEDULE_ALARM_TYPES;
import static com.dtp.core.notify.alarm.AlarmManager.doAlarm;

/**
 * DtpMonitor related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpMonitor implements ApplicationRunner, Ordered {

    private static final ScheduledExecutorService MONITOR_EXECUTOR = new ScheduledThreadPoolExecutor(
            1, new NamedThreadFactory("dtp-monitor", true));

    @Resource
    private DtpProperties dtpProperties;

    @Resource
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Override
    public void run(ApplicationArguments args) {
        MONITOR_EXECUTOR.scheduleWithFixedDelay(this::run,
                0, dtpProperties.getMonitorInterval(), TimeUnit.SECONDS);
    }

    private void run() {
        List<String> dtpNames = DtpRegistry.listAllDtpNames();
        List<String> commonNames = DtpRegistry.listAllCommonNames();
        checkAlarm(dtpNames);
        collect(dtpNames, commonNames);
    }

    private void collect(List<String> dtpNames, List<String> commonNames) {
        if (!dtpProperties.isEnabledCollect()) {
            return;
        }

        dtpNames.forEach(x -> {
            DtpExecutor executor = DtpRegistry.getDtpExecutor(x);
            ThreadPoolStats poolStats = MetricsConverter.convert(executor);
            doCollect(poolStats);
        });
        commonNames.forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getCommonExecutor(x);
            ThreadPoolStats poolStats = MetricsConverter.convert(wrapper);
            doCollect(poolStats);
        });
        publishCollectEvent();
    }

    private void checkAlarm(List<String> dtpNames) {
        dtpNames.forEach(x -> {
            DtpExecutor executor = DtpRegistry.getDtpExecutor(x);
            AlarmManager.triggerAlarm(() -> doAlarm(executor, SCHEDULE_ALARM_TYPES));
        });
        publishAlarmCheckEvent();
    }

    private void doCollect(ThreadPoolStats threadPoolStats) {
        try {
            CollectorHandler.getInstance().collect(threadPoolStats, dtpProperties.getCollectorType());
        } catch (Exception e) {
            log.error("DynamicTp monitor, metrics collect error.", e);
        }
    }

    private void publishCollectEvent() {
        CollectEvent event = new CollectEvent(this, dtpProperties);
        applicationEventMulticaster.multicastEvent(event);
    }

    private void publishAlarmCheckEvent() {
        AlarmCheckEvent event = new AlarmCheckEvent(this, dtpProperties);
        applicationEventMulticaster.multicastEvent(event);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}