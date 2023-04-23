package org.dromara.dynamictp.core.timer;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.timer.Timeout;
import org.dromara.dynamictp.common.timer.TimerTask;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import org.dromara.dynamictp.core.thread.DtpExecutor;
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
        dtpExecutor.incQueueTimeoutCount(1);
        AlarmManager.doAlarmAsync(dtpExecutor, NotifyItemEnum.QUEUE_TIMEOUT, runnable);
        log.warn("DynamicTp execute, queue timeout, tpName: {}, taskName: {}, traceId: {}",
                dtpExecutor.getThreadPoolName(), runnable.getTaskName(), runnable.getTraceId());
    }
}
