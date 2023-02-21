package com.dtp.core.thread;

import com.dtp.core.support.TaskQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * When core threads are all in busy,
 * create new thread instead of putting task into blocking queue,
 * mainly used in io intensive scenario.
 *
 * @author yanhom
 * @since 1.0.3
 **/
public class EagerDtpExecutor extends DtpExecutor {

    /**
     * The number of tasks submitted but not yet finished.
     */
    private final AtomicInteger submittedTaskCount = new AtomicInteger(0);
    
    public EagerDtpExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), new AbortPolicy());
    }
    
    public EagerDtpExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                threadFactory, new AbortPolicy());
    }
    
    public EagerDtpExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), handler);
    }
    
    public EagerDtpExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory,
                            RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public int getSubmittedTaskCount() {
        return submittedTaskCount.get();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        submittedTaskCount.decrementAndGet();
        super.afterExecute(r, t);
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }
        submittedTaskCount.incrementAndGet();
        try {
            super.execute(command);
        } catch (RejectedExecutionException rx) {
            if (getQueue() instanceof TaskQueue) {
                // If the Executor is close to maximum pool size, concurrent
                // calls to execute() may result (due to use of TaskQueue) in
                // some tasks being rejected rather than queued.
                // If this happens, add them to the queue.
                final TaskQueue queue = (TaskQueue) getQueue();
                try {
                    if (!queue.force(command, 0, TimeUnit.MILLISECONDS)) {
                        submittedTaskCount.decrementAndGet();
                        throw new RejectedExecutionException("Queue capacity is full.", rx);
                    }
                } catch (InterruptedException x) {
                    submittedTaskCount.decrementAndGet();
                    throw new RejectedExecutionException(x);
                }
            } else {
                submittedTaskCount.decrementAndGet();
                throw rx;
            }
        }
    }
}
