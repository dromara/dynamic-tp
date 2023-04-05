package com.dtp.core.support;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

/**
 * ExecutorAdapter inherits Executor, the goal of this interface is to be
 * as compatible as possible with {@link java.util.concurrent.ThreadPoolExecutor}.
 *
 * @author dragon-zhang
 * @since 1.1.3
 * @param <E> the executor type
 **/
public interface ExecutorAdapter<E extends Executor> extends Executor {

    /**
     * Get the original executor
     *
     * @return the original executor
     */
    E getOriginal();

    /**
     * Execute the task
     *
     * @param command the runnable task
     */
    @Override
    default void execute(Runnable command) {
        getOriginal().execute(command);
    }

    /**
     * Get the core pool size
     *
     * @return the core pool size
     */
    int getCorePoolSize();

    /**
     * Set the core pool size
     *
     * @param corePoolSize the core pool size
     */
    void setCorePoolSize(int corePoolSize);

    /**
     * Get the maximum pool size
     *
     * @return the maximum pool size
     */
    int getMaximumPoolSize();

    /**
     * Set the maximum pool size
     *
     * @param maximumPoolSize the maximum pool size
     */
    void setMaximumPoolSize(int maximumPoolSize);

    /**
     * Get the pool size
     *
     * @return the pool size
     */
    int getPoolSize();

    /**
     * Get the active count
     *
     * @return the active count
     */
    int getActiveCount();

    /**
     * Get the largest pool size
     *
     * @return the largest pool size
     */
    default int getLargestPoolSize() {
        //default unsupported
        return -1;
    }

    /**
     * Get the task count
     *
     * @return the task count
     */
    default long getTaskCount() {
        //default unsupported
        return -1;
    }

    /**
     * Get the completed task count
     *
     * @return the completed task count
     */
    default long getCompletedTaskCount() {
        //default unsupported
        return -1;
    }

    /**
     * Get the queue
     *
     * @return the queue
     */
    default BlockingQueue<Runnable> getQueue() {
        return new UnsupportedBlockingQueue();
    }

    /**
     * Get the rejected execution handler
     *
     * @return the rejected execution handler
     */
    default RejectedExecutionHandler getRejectedExecutionHandler() {
        //default unsupported
        return null;
    }

    /**
     * Set the rejected execution handler
     *
     * @param handler the rejected execution handler
     */
    default void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        //default unsupported
    }

    /**
     * Get the reject handler name
     *
     * @return the reject handler name
     */
    default String getRejectHandlerName() {
        return Optional.ofNullable(getRejectedExecutionHandler())
                .map(h -> h.getClass().getSimpleName())
                .orElse("unsupported");
    }

    /**
     * If allow core thread time out
     *
     * @return if allow core thread time out
     */
    default boolean allowsCoreThreadTimeOut() {
        //default unsupported
        return false;
    }

    /**
     * Allow core thread time out
     *
     * @param value if allow core thread time out
     */
    default void allowCoreThreadTimeOut(boolean value) {
        //default unsupported
    }

    /**
     * Get the keep alive time
     *
     * @param unit the time unit
     * @return the keep alive time
     */
    default long getKeepAliveTime(TimeUnit unit) {
        //default unsupported
        return -1;
    }

    /**
     * Set the keep alive time
     *
     * @param time the keep alive time
     * @param unit the time unit
     */
    default void setKeepAliveTime(long time, TimeUnit unit) {
        //default unsupported
    }
    
    class UnsupportedBlockingQueue extends AbstractQueue<Runnable> implements BlockingQueue<Runnable> {
    
        @Override
        public Iterator<Runnable> iterator() {
            throw new UnsupportedOperationException();
        }
    
        @Override
        public int size() {
            return 0;
        }
    
        @Override
        public void put(Runnable runnable) throws InterruptedException {
            throw new UnsupportedOperationException();
        }
    
        @Override
        public boolean offer(Runnable runnable, long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }
    
        @Override
        public Runnable take() {
            throw new UnsupportedOperationException();
        }
    
        @Override
        public Runnable poll(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }
    
        @Override
        public int remainingCapacity() {
            return 0;
        }
    
        @Override
        public int drainTo(Collection<? super Runnable> c) {
            throw new UnsupportedOperationException();
        }
    
        @Override
        public int drainTo(Collection<? super Runnable> c, int maxElements) {
            throw new UnsupportedOperationException();
        }
    
        @Override
        public boolean offer(Runnable runnable) {
            throw new UnsupportedOperationException();
        }
    
        @Override
        public Runnable poll() {
            throw new UnsupportedOperationException();
        }
    
        @Override
        public Runnable peek() {
            throw new UnsupportedOperationException();
        }
    }
}
