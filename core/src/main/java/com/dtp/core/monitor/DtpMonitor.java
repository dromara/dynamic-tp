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
import com.dtp.core.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.Ordered;

import java.util.Set;
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
public class DtpMonitor implements InitializingBean, Ordered, DisposableBean {

    private static final ScheduledExecutorService MONITOR_EXECUTOR = new ScheduledThreadPoolExecutor(
            1, new NamedThreadFactory("dtp-monitor", true));

    private final DtpProperties dtpProperties;

    public DtpMonitor(DtpProperties dtpProperties) {
        this.dtpProperties = dtpProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MONITOR_EXECUTOR.scheduleWithFixedDelay(this::run,
                0, dtpProperties.getMonitorInterval(), TimeUnit.SECONDS);
    }

    private void run() {
        Set<String> executorNames = DtpRegistry.listAllExecutorNames();
        checkAlarm(executorNames);
        collect(executorNames);
    }

    private void collect(Set<String> executorNames) {
        if (!dtpProperties.isEnabledCollect()) {
            return;
        }
        executorNames.forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(x);
            doCollect(MetricsConverter.convert(wrapper));
        });
        publishCollectEvent();
    }

    private void checkAlarm(Set<String> executorNames) {
        executorNames.forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(x);
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

    @Override
    public void destroy() {
        MONITOR_EXECUTOR.shutdown();
    }

}
