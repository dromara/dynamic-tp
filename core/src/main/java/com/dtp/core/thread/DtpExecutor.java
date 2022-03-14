package com.dtp.core.thread;

import com.dtp.common.dto.NotifyItem;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.reject.RejectHandlerGetter;
import com.dtp.core.spring.DtpLifecycleSupport;
import com.dtp.core.support.TaskWrapper;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dynamic ThreadPoolExecutor inherits DtpLifecycleSupport, and extends some features.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class DtpExecutor extends DtpLifecycleSupport {

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

    /**
     * Task wrapper, do sth enhanced.
     */
    private TaskWrapper taskWrapper;

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

    @Override
    public void execute(Runnable command) {
        if (Objects.nonNull(taskWrapper)) {
            command = taskWrapper.wrap(command);
        }
        super.execute(command);
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

    public void setTaskWrapper(TaskWrapper taskWrapper) {
        this.taskWrapper = taskWrapper;
    }
}
