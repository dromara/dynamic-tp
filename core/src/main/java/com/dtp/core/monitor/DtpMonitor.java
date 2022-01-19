package com.dtp.core.monitor;

import com.dtp.core.DtpExecutor;
import com.dtp.core.DtpRegistry;
import com.dtp.core.handler.CollectorHandler;
import com.dtp.core.notify.AlarmManager;
import com.dtp.core.thread.NamedThreadFactory;
import com.google.common.collect.Lists;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.em.NotifyTypeEnum;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import javax.annotation.Resource;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.dtp.core.notify.AlarmManager.doAlarm;

/**
 * DtpMonitor related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpMonitor implements ApplicationRunner {

    private static final ScheduledExecutorService MONITOR_EXECUTOR = new ScheduledThreadPoolExecutor(
            1,
            new NamedThreadFactory("dtp-monitor", true));

    @Resource
    private DtpProperties dtpProperties;

    @Override
    public void run(ApplicationArguments args) {
        MONITOR_EXECUTOR.scheduleWithFixedDelay(this::run,
                0, dtpProperties.getMonitorInterval(), TimeUnit.SECONDS);
    }

    private void run() {
        try {
            val names = DtpRegistry.listAllDtpNames();
            names.forEach(x -> {
                DtpExecutor executor = DtpRegistry.getExecutor(x);
                AlarmManager.triggerAlarm(() -> doAlarm(executor,
                                Lists.newArrayList(NotifyTypeEnum.LIVENESS, NotifyTypeEnum.CAPACITY)));

                if (dtpProperties.isEnabledCollect()) {
                    CollectorHandler.getInstance().collect(executor, dtpProperties.getCollectorType());
                }
            });
        } catch (Exception e) {
            log.error("DynamicTp monitor, run error...", e);
        }
    }
}