package com.dtp.core.thread;

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
 **/
public interface ExecutorAdapter<E extends Executor> extends Executor {
    
    E getOriginal();
    
    @Override
    default void execute(Runnable command) {
        getOriginal().execute(command);
    }
    
    int getCorePoolSize();
    
    void setCorePoolSize(int corePoolSize);
    
    int getMaximumPoolSize();
    
    void setMaximumPoolSize(int maximumPoolSize);
    
    int getPoolSize();
    
    int getActiveCount();
    
    default int getLargestPoolSize() {
        //default unsupported
        return -1;
    }
    
    default long getTaskCount() {
        //default unsupported
        return -1;
    }
    
    default long getCompletedTaskCount() {
        //default unsupported
        return -1;
    }
    
    default BlockingQueue<Runnable> getQueue() {
        return new UnsupportedBlockingQueue();
    }
    
    default RejectedExecutionHandler getRejectedExecutionHandler() {
        //default unsupported
        return null;
    }
    
    default void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        //default unsupported
    }
    
    default String getRejectHandlerName() {
        return Optional.ofNullable(getRejectedExecutionHandler())
                .map(h -> h.getClass().getSimpleName())
                .orElse("unsupported");
    }
    
    default boolean allowsCoreThreadTimeOut() {
        //default unsupported
        return false;
    }
    
    default void allowCoreThreadTimeOut(boolean value) {
        //default unsupported
    }
    
    default long getKeepAliveTime(TimeUnit unit) {
        //default unsupported
        return -1;
    }
    
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
