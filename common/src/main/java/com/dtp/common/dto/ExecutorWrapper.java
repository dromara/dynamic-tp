package com.dtp.common.dto;

import com.dtp.common.em.NotifyTypeEnum;
import lombok.Data;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Executor wrapper
 *
 * @author: yanhom
 * @since 1.0.3
 **/
@Data
public class ExecutorWrapper {

    private String threadPoolName;

    private Executor executor;

    /**
     * Notify items, see {@link NotifyTypeEnum}.
     */
    private List<NotifyItem> notifyItems;

    public ExecutorWrapper(String threadPoolName, Executor executor) {
        this.threadPoolName = threadPoolName;
        this.executor = executor;
        this.notifyItems = NotifyItem.getSimpleNotifyItems();
    }

    public ExecutorWrapper(String threadPoolName, Executor executor, List<NotifyItem> notifyItems) {
        this.threadPoolName = threadPoolName;
        this.executor = executor;
        this.notifyItems = notifyItems;
    }
}
