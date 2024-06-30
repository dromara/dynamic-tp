package org.dromara.dynamictp.core.spring;

import org.springframework.context.SmartLifecycle;

public class DtpLifecycleSpringAdapter implements SmartLifecycle {
    private final LifeCycleManagement lifeCycleManagement;
    private boolean isRunning = false;

    public DtpLifecycleSpringAdapter(LifeCycleManagement lifeCycleManagement) {
        this.lifeCycleManagement = lifeCycleManagement;
    }

    @Override
    public void start() {
        lifeCycleManagement.start();
        isRunning = true;
    }

    @Override
    public void stop() {
        lifeCycleManagement.stop();
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    public void stop(Runnable callback) {
        lifeCycleManagement.stop();
        callback.run();
        isRunning = false;
    }

    @Override
    public boolean isAutoStartup() {
        return lifeCycleManagement.isAutoStartup();
    }

    @Override
    public int getPhase() {
        return lifeCycleManagement.getPhase();
    }
}
