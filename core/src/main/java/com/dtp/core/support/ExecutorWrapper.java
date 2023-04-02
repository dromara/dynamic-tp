package com.dtp.core.support;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.entity.NotifyItem;
import com.dtp.core.thread.DtpExecutor;
import lombok.Data;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Executor wrapper
 *
 * @author yanhom
 * @since 1.0.3
 **/
@Data
public class ExecutorWrapper {

    private String threadPoolName;

    private String threadPoolAliasName;

    private ExecutorAdapter<?> executor;

    /**
     * Notify items, see {@link NotifyItemEnum}.
     */
    private List<NotifyItem> notifyItems;

    /**
     * Notify platform ids.
     */
    private List<String> platformIds;

    private boolean notifyEnabled = true;

    public ExecutorWrapper(DtpExecutor executor) {
        this.threadPoolName = executor.getThreadPoolName();
        this.executor = executor;
        this.notifyItems = executor.getNotifyItems();
        this.notifyEnabled = executor.isNotifyEnabled();
    }

    public ExecutorWrapper(String threadPoolName, Executor executor) {
        this.threadPoolName = threadPoolName;
        if (executor instanceof ThreadPoolExecutor) {
            this.executor = new ThreadPoolExecutorAdapter((ThreadPoolExecutor) executor);
        } else if (executor instanceof ExecutorAdapter<?>) {
            this.executor = (ExecutorAdapter<?>) executor;
        } else {
            throw new IllegalArgumentException("unsupported Executor type !");
        }
        this.notifyItems = NotifyItem.getSimpleNotifyItems();
    }

    public static ExecutorWrapper of(DtpExecutor executor) {
        return new ExecutorWrapper(executor);
    }
}
