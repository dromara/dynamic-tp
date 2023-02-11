package com.dtp.core.thread;

import com.dtp.core.reject.RejectHandlerGetter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.dtp.common.em.QueueTypeEnum.buildLbq;

/**
 * {@link OrderedDtpExecutor} can ensure that the delivered tasks are executed
 * according to the key and task submission order. It is applicable to scenarios
 * where the throughput is improved through parallel processing and the tasks
 * are run in a certain order.
 *
 * @author dragon-zhang
 */
@Slf4j
public class OrderedDtpExecutor extends DtpExecutor {
    
    protected final AtomicInteger count = new AtomicInteger(0);
    
    protected final List<DtpExecutor> executors = new ArrayList<>();
    
    public OrderedDtpExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        for (int i = 0; i < corePoolSize; i++) {
            executors.add(new DtpExecutor(1, 1, keepAliveTime, unit,
                    buildLbq(getQueueName(), getQueueCapacity()), buildThreadFactory(i),
                    RejectHandlerGetter.getProxy(handler)));
        }
    }
    
    @Override
    public void execute(Runnable command) {
        execute(null, command);
    }
    
    public void execute(Object arg, Runnable command) {
        choose(arg).execute(command);
    }
    
    @Override
    public Future<?> submit(Runnable task) {
        return submit((Object) null, task);
    }
    
    public Future<?> submit(Object arg, Runnable task) {
        return choose(arg).submit(task);
    }
    
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return submit(null, task, result);
    }
    
    public <T> Future<T> submit(Object arg, Runnable task, T result) {
        return choose(arg).submit(task, result);
    }
    
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return submit((Object) null, task);
    }
    
    public <T> Future<T> submit(Object arg, Callable<T> task) {
        return choose(arg).submit(task);
    }
    
    protected DtpExecutor choose(Object arg) {
        int size = this.executors.size();
        if (size == 1) {
            return this.executors.get(0);
        }
        int index;
        if (Objects.isNull(arg)) {
            int i = count.getAndIncrement();
            if (i < 0) {
                i = 0;
                count.set(0);
            }
            index = i;
        } else {
            index = arg.hashCode();
        }
        return this.executors.get(index % size);
    }
    
    @Override
    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < this.executors.size()) {
            log.error("Except corePoolSize must >= {}, newCorePoolSize: {}, threadPoolName: {}",
                    this.executors.size(), corePoolSize, threadPoolName);
            return;
        }
        for (int i = this.executors.size(); i < corePoolSize; i++) {
            this.executors.add(new DtpExecutor(1, 1,
                    getKeepAliveTime(TimeUnit.SECONDS), TimeUnit.SECONDS,
                    buildLbq(getQueueName(), getQueueCapacity()), buildThreadFactory(i),
                    RejectHandlerGetter.getProxy(getRejectHandlerName())));
        }
    }
    
    @Override
    public int getCorePoolSize() {
        return this.executors.size();
    }
    
    @Override
    public final int getPoolSize() {
        return getCorePoolSize();
    }
    
    @Override
    public final int getMaximumPoolSize() {
        return getCorePoolSize();
    }

    @Override
    public void shutdown() {
        for (ExecutorService executor : this.executors) {
            executor.shutdown();
        }
    }
    
    @Override
    public List<Runnable> shutdownNow() {
        return this.executors.stream()
                .map(ExecutorService::shutdownNow)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean isShutdown() {
        boolean result = true;
        for (ExecutorService executor : this.executors) {
            result = result && executor.isShutdown();
        }
        return result;
    }
    
    @Override
    public boolean isTerminated() {
        boolean result = true;
        for (ExecutorService executor : this.executors) {
            result = result && executor.isTerminated();
        }
        return result;
    }
    
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        boolean result = true;
        for (ExecutorService executor : this.executors) {
            result = result && executor.awaitTermination(timeout, unit);
        }
        return result;
    }

    private ThreadFactory buildThreadFactory(int index) {
        if (getThreadFactory() instanceof NamedThreadFactory) {
            String prefix = ((NamedThreadFactory) getThreadFactory()).getNamePrefix() + "#" + index;
            return new NamedThreadFactory(prefix);
        }
        return getThreadFactory();
    }
}
