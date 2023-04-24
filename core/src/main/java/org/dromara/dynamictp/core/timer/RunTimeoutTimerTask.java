package org.dromara.dynamictp.core.timer;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.timer.Timeout;
import org.dromara.dynamictp.common.timer.TimerTask;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import org.dromara.dynamictp.core.thread.DtpExecutor;
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
        dtpExecutor.incRunTimeoutCount(1);
        AlarmManager.doAlarmAsync(dtpExecutor, NotifyItemEnum.RUN_TIMEOUT, runnable);
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
