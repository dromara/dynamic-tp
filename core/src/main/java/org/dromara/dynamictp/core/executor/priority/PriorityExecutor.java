package org.dromara.dynamictp.core.executor.priority;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
@Slf4j
public class PriorityExecutor extends DtpExecutor {

    public PriorityExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            int capacity) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<>(capacity), Executors.defaultThreadFactory(), new AbortPolicy());
    }

    public PriorityExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            int capacity,
                            ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<>(capacity), threadFactory, new AbortPolicy());
    }

    public PriorityExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            int capacity,
                            RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<>(capacity), Executors.defaultThreadFactory(), handler);
    }

    public PriorityExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            int capacity,
                            ThreadFactory threadFactory,
                            RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<>(capacity), threadFactory, handler);
    }


    public PriorityExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            PriorityBlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), new AbortPolicy());
    }

    public PriorityExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            PriorityBlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new AbortPolicy());
    }

    public PriorityExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            PriorityBlockingQueue<Runnable> workQueue,
                            RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
    }

    public PriorityExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            PriorityBlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory,
                            RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }


    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new PriorityFutureTask<>(runnable, value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new PriorityFutureTask<>(callable);
    }

}
