package com.dtp.core.support;

import com.dtp.common.entity.NotifyItem;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.core.thread.DtpExecutor;
import lombok.Data;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Executor wrapper
 *
 * @author yanhom
 * @since 1.0.3
 **/
@Data
public class ExecutorWrapper {

    private String threadPoolName;

    private Executor executor;

    private String threadPoolAliasName;

    /**
     * Notify items, see {@link NotifyItemEnum}.
     */
    private List<NotifyItem> notifyItems;

    private boolean notifyEnabled = true;

    public ExecutorWrapper(String threadPoolName, Executor executor) {
        this.threadPoolName = threadPoolName;
        this.executor = executor;
        this.notifyItems = NotifyItem.getSimpleNotifyItems();
    }

    public ExecutorWrapper(DtpExecutor executor) {
        this.threadPoolName = executor.getThreadPoolName();
        this.executor = executor;
        this.notifyItems = executor.getNotifyItems();
        this.notifyEnabled = executor.isNotifyEnabled();
    }
}
