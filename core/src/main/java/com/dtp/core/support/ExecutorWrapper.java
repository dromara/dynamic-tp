package com.dtp.core.support;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.entity.NotifyItem;
import com.dtp.core.notifier.capture.CapturedExecutor;
import com.dtp.core.thread.DtpExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

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
@Slf4j
public class ExecutorWrapper {

    /**
     * Thread pool name.
     */
    private String threadPoolName;

    /**
     * Thread pool alias name.
     */
    private String threadPoolAliasName;

    /**
     * Executor.
     */
    private ExecutorAdapter<?> executor;

    /**
     * Notify items, see {@link NotifyItemEnum}.
     */
    private List<NotifyItem> notifyItems;

    /**
     * Notify platform ids.
     */
    private List<String> platformIds;

    /**
     * Whether to enable notification.
     */
    private boolean notifyEnabled = true;

    public ExecutorWrapper() {
    }

    public ExecutorWrapper(DtpExecutor executor) {
        this.threadPoolName = executor.getThreadPoolName();
        this.threadPoolAliasName = executor.getThreadPoolAliasName();
        this.executor = executor;
        this.notifyItems = executor.getNotifyItems();
        this.notifyEnabled = executor.isNotifyEnabled();
        this.platformIds = executor.getPlatformIds();
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

    public ExecutorWrapper capture() {
        ExecutorWrapper executorWrapper = new ExecutorWrapper();
        BeanUtils.copyProperties(this, executorWrapper);
        executorWrapper.executor = new CapturedExecutor(this.getExecutor());
        return executorWrapper;
    }
}
