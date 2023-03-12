package com.dtp.common.entity;

import com.dtp.common.constant.DynamicTpConst;
import com.dtp.common.em.NotifyItemEnum;
import lombok.Data;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPool base properties, mainly used for adapter module.
 *
 * @author yanhom
 * @since 1.0.6
 **/
@Data
public class TpExecutorProps {

    /**
     * Name of ThreadPool.
     */
    private String threadPoolName;

    /**
     * Simple Alias Name of  ThreadPool. Use for notify.
     */
    private String threadPoolAliasName;

    /**
     * CoreSize of ThreadPool.
     */
    private int corePoolSize = 1;

    /**
     * MaxSize of ThreadPool.
     */
    private int maximumPoolSize = DynamicTpConst.AVAILABLE_PROCESSORS;

    /**
     * When the number of threads is greater than the core,
     * this is the maximum time that excess idle threads
     * will wait for new tasks before terminating.
     */
    private long keepAliveTime = 60;

    /**
     * Timeout unit.
     */
    private TimeUnit unit = TimeUnit.SECONDS;

    /**
     * Notify items, see {@link NotifyItemEnum}
     */
    private List<NotifyItem> notifyItems;

    /**
     * Notify platform id
     */
    private List<String> platformIds;

    /**
     * If enable notify.
     */
    private boolean notifyEnabled = true;
}
