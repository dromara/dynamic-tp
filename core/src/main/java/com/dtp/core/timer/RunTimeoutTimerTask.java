package com.dtp.core.timer;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.timer.Timeout;
import com.dtp.common.timer.TimerTask;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.support.runnable.DtpRunnable;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * A timer task used to handle run timeout.
 *
 * @author kamtohung
 **/
@Slf4j
public class RunTimeoutTimerTask implements TimerTask {

    private final DtpExecutor dtpExecutor;

    private final DtpRunnable runnable;

    private final Thread thread;

    public RunTimeoutTimerTask(DtpExecutor dtpExecutor, DtpRunnable runnable, Thread thread) {
        this.dtpExecutor = dtpExecutor;
        this.runnable = runnable;
        this.thread = thread;
    }

    @Override
    public void run(Timeout timeout) {
        dtpExecutor.getRunTimeoutCount().increment();
        AlarmManager.doAlarmAsync(dtpExecutor, NotifyItemEnum.RUN_TIMEOUT);
        log.warn("DynamicTp execute, run timeout, tpName: {}, taskName: {}, traceId: {}, stackTrace: {}",
                dtpExecutor.getThreadPoolName(), runnable.getTaskName(),
                runnable.getTraceId(), traceToString(thread.getStackTrace()));
    }

    public String traceToString(StackTraceElement[] trace) {
        StringBuilder builder = new StringBuilder(512);
        builder.append("\n");
        for (StackTraceElement traceElement : trace) {
            builder.append("\tat ").append(traceElement).append("\n");
        }
        return builder.toString();
    }
}
