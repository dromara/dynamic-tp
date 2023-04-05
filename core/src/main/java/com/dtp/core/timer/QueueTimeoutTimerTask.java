package com.dtp.core.timer;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.timer.Timeout;
import com.dtp.common.timer.TimerTask;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.support.runnable.DtpRunnable;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * A timer task used to handle queued timeout.
 *
 * @author kamtohung
 **/
@Slf4j
public class QueueTimeoutTimerTask implements TimerTask {

    private final DtpExecutor dtpExecutor;

    private final DtpRunnable runnable;

    public QueueTimeoutTimerTask(DtpExecutor dtpExecutor, DtpRunnable runnable) {
        this.dtpExecutor = dtpExecutor;
        this.runnable = runnable;
    }

    @Override
    public void run(Timeout timeout) {
        dtpExecutor.getQueueTimeoutCount().increment();
        AlarmManager.doAlarmAsync(dtpExecutor, NotifyItemEnum.QUEUE_TIMEOUT, runnable);
        log.warn("DynamicTp execute, queue timeout, tpName: {}, taskName: {}, traceId: {}",
                dtpExecutor.getThreadPoolName(), runnable.getTaskName(), runnable.getTraceId());
    }
}
