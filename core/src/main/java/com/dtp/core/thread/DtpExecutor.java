package com.dtp.core.thread;

import com.dtp.common.dto.NotifyItem;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.reject.RejectHandlerGetter;

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
     * RejectHandler name.
     */
    private String rejectHandlerName;

    /**
     * Notify items, see {@link NotifyTypeEnum}.
     */
    private List<NotifyItem> notifyItems;

    public DtpExecutor(int corePoolSize,
                       int maximumPoolSize,
                       long keepAliveTime,
                       TimeUnit unit,
                       BlockingQueue<Runnable> workQueue,
                       ThreadFactory threadFactory,
                       RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.rejectHandlerName = handler.getClass().getSimpleName();
        RejectedExecutionHandler rejectedExecutionHandler = RejectHandlerGetter.getProxy(handler);
        setRejectedExecutionHandler(rejectedExecutionHandler);
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
        return rejectHandlerName;
    }

    public void setRejectHandlerName(String rejectHandlerName) {
        this.rejectHandlerName = rejectHandlerName;
    }
}
