package com.dtp.core.thread;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default ThreadFactory used in Dynamic ThreadPoolExecutors.
 *
 * @author: yanhom
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

    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public NamedThreadFactory(String namePrefix, boolean daemon, int priority) {
        this.daemon = daemon;
        this.priority = priority;
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
        this.uncaughtExceptionHandler = new DtpUncaughtExceptionHandler();
    }

    public NamedThreadFactory(String namePrefix) {
        this(namePrefix, false, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(String namePrefix, boolean daemon) {
        this(namePrefix, daemon, Thread.NORM_PRIORITY);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, StrUtil.format("{}{}", namePrefix, seq.getAndIncrement()));
        t.setDaemon(daemon);
        t.setPriority(priority);
        t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return t;
    }

    public static class DtpUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            log.error("thread {} throw exception {}", t, e);
        }
    }
}
