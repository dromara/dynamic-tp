package org.dromara.dynamictp.core.spring;

public interface LifeCycleManagement {
    void start();
    void stop();
    boolean isRunning();
    void stop(Runnable callback);
    boolean isAutoStartup();
    int getPhase();
    void shutdownInternal();
}
