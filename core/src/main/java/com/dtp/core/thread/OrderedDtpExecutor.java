package com.dtp.core.thread;

import com.dtp.core.notifier.manager.NotifyHelper;
import com.dtp.core.reject.RejectHandlerGetter;
import com.dtp.core.support.ThreadPoolBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    
    protected final Map<Integer, DtpExecutor> executors = new ConcurrentHashMap<>();
    
    public OrderedDtpExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), new AbortPolicy());
    }
    
    public OrderedDtpExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                threadFactory, new AbortPolicy());
    }
    
    public OrderedDtpExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), handler);
    }
    
    public OrderedDtpExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        for (int i = 0; i < corePoolSize; i++) {
            DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                    .corePoolSize(1)
                    .maximumPoolSize(1)
                    .keepAliveTime(keepAliveTime)
                    .timeUnit(unit)
                    .workQueue(getQueueName(), getQueueCapacity())
                    .threadFactory(((NamedThreadFactory) getThreadFactory()).getNamePrefix() + "#" + i)
                    .rejectedExecutionHandler(handler)
                    .buildDynamic();
            executors.put(i, executor);
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
    
    public DtpExecutor choose(Object arg) {
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
    protected void initialize() {
        executors.forEach((k, v) -> {
            NotifyHelper.initNotify(v);
            if (isPreStartAllCoreThreads()) {
                v.prestartAllCoreThreads();
            }
            populateExecutor(v, k);
            // reset reject handler in initialize phase according to rejectEnhanced
            v.setRejectHandler(RejectHandlerGetter.buildRejectedHandler(getRejectHandlerName()));
        });
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < this.executors.size()) {
            log.error("Except corePoolSize must >= {}, newCorePoolSize: {}, threadPoolName: {}",
                    this.executors.size(), corePoolSize, threadPoolName);
            return;
        }
        for (int i = this.executors.size(); i < corePoolSize; i++) {
            DtpExecutor executor = ThreadPoolBuilder.newBuilder()
                    .corePoolSize(1)
                    .maximumPoolSize(1)
                    .keepAliveTime(getKeepAliveTime(TimeUnit.SECONDS))
                    .timeUnit(TimeUnit.SECONDS)
                    .workQueue(getQueueName(), getQueueCapacity())
                    .threadFactory(((NamedThreadFactory) getThreadFactory()).getNamePrefix() + "#" + i)
                    .rejectedExecutionHandler(getRejectHandlerName())
                    .buildDynamic();
            this.executors.put(i, executor);
        }
    }
    
    @Override
    public int getCorePoolSize() {
        return this.executors.size();
    }
    
    @Override
    public final int getMaximumPoolSize() {
        return getCorePoolSize();
    }

    @Override
    public final int getPoolSize() {
        int poolSize = 0;
        for (DtpExecutor executor : this.executors.values()) {
            poolSize += executor.getPoolSize();
        }
        return poolSize;
    }

    @Override
    public int getActiveCount() {
        int activeCount = 0;
        for (DtpExecutor executor : this.executors.values()) {
            activeCount += executor.getActiveCount();
        }
        return activeCount;
    }

    @Override
    public int getLargestPoolSize() {
        int largestPoolSize = 0;
        for (DtpExecutor executor : this.executors.values()) {
            largestPoolSize += executor.getLargestPoolSize();
        }
        return largestPoolSize;
    }

    @Override
    public long getTaskCount() {
        long taskCount = 0;
        for (DtpExecutor executor : this.executors.values()) {
            taskCount += executor.getTaskCount();
        }
        return taskCount;
    }

    @Override
    public long getCompletedTaskCount() {
        long completedTaskCount = 0;
        for (DtpExecutor executor : this.executors.values()) {
            completedTaskCount += executor.getCompletedTaskCount();
        }
        return completedTaskCount;
    }

    @Override
    public long getRejectCount() {
        long rejectCount = 0;
        for (DtpExecutor executor : this.executors.values()) {
            rejectCount += executor.getRejectCount();
        }
        return rejectCount;
    }

    @Override
    public long getRunTimeoutCount() {
        long runTimeoutCount = 0;
        for (DtpExecutor executor : this.executors.values()) {
            runTimeoutCount += executor.getRunTimeoutCount();
        }
        return runTimeoutCount;
    }

    @Override
    public long getQueueTimeoutCount() {
        long queueTimeoutCount = 0;
        for (DtpExecutor executor : this.executors.values()) {
            queueTimeoutCount += executor.getQueueTimeoutCount();
        }
        return queueTimeoutCount;
    }

    @Override
    public int getQueueSize() {
        int queueSize = 0;
        for (DtpExecutor executor : this.executors.values()) {
            queueSize += executor.getQueueSize();
        }
        return queueSize;
    }

    @Override
    public int getQueueRemainingCapacity() {
        int queueRemainingCapacity = 0;
        for (DtpExecutor executor : this.executors.values()) {
            queueRemainingCapacity += executor.getQueueRemainingCapacity();
        }
        return queueRemainingCapacity;
    }

    @Override
    public void shutdown() {
        for (DtpExecutor executor : this.executors.values()) {
            executor.shutdown();
        }
    }
    
    @Override
    public List<Runnable> shutdownNow() {
        return this.executors.values().stream()
                .map(ExecutorService::shutdownNow)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean isShutdown() {
        boolean result = true;
        for (DtpExecutor executor : this.executors.values()) {
            result = result && executor.isShutdown();
        }
        return result;
    }
    
    @Override
    public boolean isTerminated() {
        boolean result = true;
        for (DtpExecutor executor : this.executors.values()) {
            result = result && executor.isTerminated();
        }
        return result;
    }
    
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        boolean result = true;
        for (DtpExecutor executor : this.executors.values()) {
            result = result && executor.awaitTermination(timeout, unit);
        }
        return result;
    }

    private void populateExecutor(DtpExecutor executor, int idx) {
        executor.setTaskWrappers(getTaskWrappers());
        executor.setNotifyItems(getNotifyItems());
        executor.setPlatformIds(getPlatformIds());
        executor.setNotifyEnabled(isNotifyEnabled());
        executor.setRejectEnhanced(isRejectEnhanced());
        executor.setQueueTimeout(getQueueTimeout());
        executor.setRunTimeout(getRunTimeout());
        executor.setAwaitTerminationSeconds(getAwaitTerminationSeconds());
        executor.setWaitForTasksToCompleteOnShutdown(isWaitForTasksToCompleteOnShutdown());
        executor.setThreadPoolName(getThreadPoolName() + "-" + idx);
        executor.setAllowCoreThreadTimeOut(allowsCoreThreadTimeOut());
        executor.setThreadPoolAliasName(getThreadPoolAliasName());
    }
}
