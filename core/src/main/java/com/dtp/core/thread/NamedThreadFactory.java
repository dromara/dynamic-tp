package com.dtp.core.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default ThreadFactory used in Dynamic ThreadPoolExecutor.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class NamedThreadFactory implements ThreadFactory {

    private final ThreadGroup group;

    private final String namePrefix;

    /**
     * is daemon thread.
     */
    private final boolean daemon;

    /**
     * thread priority.
     */
    private final Integer priority;

    /**
     * thread name index.
     */
    private final AtomicInteger seq = new AtomicInteger(1);

    public NamedThreadFactory(String namePrefix, boolean daemon, int priority) {
        this.daemon = daemon;
        this.priority = priority;
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    public NamedThreadFactory(String namePrefix) {
        this(namePrefix, false, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(String namePrefix, boolean daemon) {
        this(namePrefix, daemon, Thread.NORM_PRIORITY);
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = namePrefix + "-" + seq.getAndIncrement();
        Thread t = new Thread(group, r, name);
        t.setDaemon(daemon);
        t.setPriority(priority);
        return t;
    }

    public String getNamePrefix() {
        return namePrefix;
    }
}
