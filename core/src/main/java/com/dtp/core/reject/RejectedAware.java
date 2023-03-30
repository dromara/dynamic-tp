package com.dtp.core.reject;

import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.support.runnable.DtpRunnable;
import com.dtp.core.support.runnable.NamedRunnable;
import com.dtp.core.thread.DtpExecutor;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.concurrent.ThreadPoolExecutor;

import static com.dtp.common.constant.DynamicTpConst.TRACE_ID;
import static com.dtp.common.em.NotifyItemEnum.REJECT;

/**
 * RejectedAware related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public interface RejectedAware {

    /**
     * Do sth before reject.
     *
     * @param runnable Runnable instance
     * @param executor ThreadPoolExecutor instance
     * @param log      logger
     */
    default void beforeReject(Runnable runnable, ThreadPoolExecutor executor, Logger log) {
        if (executor instanceof DtpExecutor) {
            if (runnable instanceof DtpRunnable) {
                ((DtpRunnable) runnable).cancelQueueTimeoutTask();
                log.warn("DynamicTp execute, thread pool is exhausted, cancel queue timeout task, traceId: {}", MDC.get(TRACE_ID));
            }
            DtpExecutor dtpExecutor = (DtpExecutor) executor;
            dtpExecutor.incRejectCount(1);
            AlarmManager.doAlarmAsync(dtpExecutor, REJECT);
            String taskName = (runnable instanceof NamedRunnable) ? ((NamedRunnable) runnable).getName() : null;
            log.warn("DynamicTp execute, thread pool is exhausted, tpName: {}, taskName: {}, traceId: {}, " +
                            "poolSize: {} (active: {}, core: {}, max: {}, largest: {}), " +
                            "task: {} (completed: {}), queueCapacity: {}, (currSize: {}, remaining: {}), " +
                            "executorStatus: (isShutdown: {}, isTerminated: {}, isTerminating: {})",
                    dtpExecutor.getThreadPoolName(), taskName, MDC.get(TRACE_ID), executor.getPoolSize(),
                    executor.getActiveCount(), executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                    executor.getLargestPoolSize(), executor.getTaskCount(), executor.getCompletedTaskCount(),
                    dtpExecutor.getQueueCapacity(), dtpExecutor.getQueue().size(), executor.getQueue().remainingCapacity(),
                    executor.isShutdown(), executor.isTerminated(), executor.isTerminating());
        }
    }
}
