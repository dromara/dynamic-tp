package org.dromara.dynamictp.core.spring;

import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.monitor.DtpMonitor;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.notifier.manager.NoticeManager;
import org.dromara.dynamictp.core.support.DtpLifecycleSupport;
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

    public void shutdownInternal() {
        DtpMonitor.destroy();
        AlarmManager.destroy();
        NoticeManager.destroy();
    }
}
