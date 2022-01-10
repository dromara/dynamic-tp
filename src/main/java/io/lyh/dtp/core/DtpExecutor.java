package io.lyh.dtp.core;

import io.lyh.dtp.common.em.NotifyTypeEnum;
import io.lyh.dtp.notify.NotifyItem;
import io.lyh.dtp.handler.reject.RejectedCountableCallerRunsPolicy;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dynamic ThreadPoolExecutor inherits ThreadPoolExecutor, and extends some features.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class DtpExecutor extends ThreadPoolExecutor {

    /**
     * Uniquely identifies.
     */
    private String threadPoolName;

    /**
     * Total reject count.
     */
    private final AtomicInteger rejectCount = new AtomicInteger();

    /**
     * Notify items, see {@link NotifyTypeEnum}.
     */
    private List<NotifyItem> notifyItems;

    public DtpExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public DtpExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                       BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new RejectedCountableCallerRunsPolicy());
    }

    public DtpExecutor(int corePoolSize,
                       int maximumPoolSize,
                       long keepAliveTime,
                       TimeUnit unit,
                       BlockingQueue<Runnable> workQueue,
                       ThreadFactory threadFactory,
                       RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(command);
    }

    public void setThreadPoolName(String threadPoolName) {
        this.threadPoolName = threadPoolName;
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public void incRejectCount(int count) {
        rejectCount.addAndGet(count);
    }

    public int getRejectCount() {
        return rejectCount.get();
    }

    public List<NotifyItem> getNotifyItems() {
        return notifyItems;
    }

    public void setNotifyItems(List<NotifyItem> notifyItems) {
        this.notifyItems = notifyItems;
    }

    public String getQueueName() {
        return getQueue().getClass().getSimpleName();
    }

    public int getQueueCapacity() {
        return getQueue().size() + getQueue().remainingCapacity();
    }

    public String getRejectHandlerName() {
        return getRejectedExecutionHandler().getClass().getSimpleName();
    }
}
