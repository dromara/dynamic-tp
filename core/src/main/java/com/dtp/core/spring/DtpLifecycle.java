package com.dtp.core.spring;

import com.dtp.core.DtpRegistry;
import com.dtp.core.monitor.DtpMonitor;
import com.dtp.core.notifier.manager.AlarmManager;
import com.dtp.core.notifier.manager.NoticeManager;
import com.dtp.core.support.DtpLifecycleSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DtpLifecycle related
 *
 * @author yanhom
 * @since 1.1.3
 **/
@Slf4j
public class DtpLifecycle implements SmartLifecycle {

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start() {
        if (this.running.compareAndSet(false, true)) {
            DtpRegistry.listAllExecutors().forEach((k, v) -> DtpLifecycleSupport.initialize(v));
            initializeInternal();
        }
    }

    @Override
    public void stop() {
        if (this.running.compareAndSet(true, false)) {
            shutdownInternal();
            DtpRegistry.listAllExecutors().forEach((k, v) -> DtpLifecycleSupport.destroy(v));
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

     public void initializeInternal() {
        DtpMonitor.initialize();
        AlarmManager.initialize();
        NoticeManager.initialize();
     }

    public void shutdownInternal() {
        DtpMonitor.destroy();
        AlarmManager.destroy();
        NoticeManager.destroy();
    }
}
