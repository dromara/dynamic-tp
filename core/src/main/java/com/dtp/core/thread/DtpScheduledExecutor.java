package com.dtp.core.thread;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;


/**
 * Support ScheduledThreadExecutor.
 *
 * @author windsearcher
 **/
public class DtpScheduledExecutor extends DtpExecutor {

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public DtpScheduledExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory,
                            RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory, handler);
    }

    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay,
                                       TimeUnit unit) {
        return scheduledThreadPoolExecutor.schedule(command, delay, unit);
    }


    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit) {

        return scheduledThreadPoolExecutor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit) {
        return scheduledThreadPoolExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public void execute(Runnable command) {
        schedule(command, 0, NANOSECONDS);
    }
}
