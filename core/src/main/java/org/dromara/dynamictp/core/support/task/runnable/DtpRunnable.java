package org.dromara.dynamictp.core.support.task.runnable;

import org.dromara.dynamictp.common.ApplicationContextHolder;
import org.dromara.dynamictp.common.timer.HashedWheelTimer;
import org.dromara.dynamictp.common.timer.Timeout;
import org.dromara.dynamictp.core.thread.DtpExecutor;
import org.dromara.dynamictp.core.timer.QueueTimeoutTimerTask;
import org.dromara.dynamictp.core.timer.RunTimeoutTimerTask;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;

/**
 * DtpRunnable related
 *
 * @author yanhom
 * @since 1.0.4
 */
public class DtpRunnable implements Runnable {

    private final Runnable runnable;

    private final String taskName;

    private final String traceId;

    private Timeout runTimeoutTimer;

    private Timeout queueTimeoutTimer;

    public DtpRunnable(Runnable runnable, String taskName) {
        this.runnable = runnable;
        this.taskName = taskName;
        this.traceId = MDC.get(TRACE_ID);
    }

    public void startQueueTimeoutTask(DtpExecutor executor) {
        long queueTimeout = executor.getQueueTimeout();
        if (queueTimeout <= 0) {
            return;
        }
        HashedWheelTimer hashedWheelTimer = ApplicationContextHolder.getBean(HashedWheelTimer.class);
        QueueTimeoutTimerTask queueTimeoutTimerTask = new QueueTimeoutTimerTask(executor, this);
        queueTimeoutTimer = hashedWheelTimer.newTimeout(queueTimeoutTimerTask, queueTimeout, TimeUnit.MILLISECONDS);
    }

    public void cancelQueueTimeoutTask() {
        if (queueTimeoutTimer != null) {
            queueTimeoutTimer.cancel();
        }
    }

    public void startRunTimeoutTask(DtpExecutor executor, Thread thread) {
        long runTimeout = executor.getRunTimeout();
        if (runTimeout <= 0) {
            return;
        }
        HashedWheelTimer hashedWheelTimer = ApplicationContextHolder.getBean(HashedWheelTimer.class);
        RunTimeoutTimerTask runTimeoutTimerTask = new RunTimeoutTimerTask(executor, this, thread);
        runTimeoutTimer = hashedWheelTimer.newTimeout(runTimeoutTimerTask, runTimeout, TimeUnit.MILLISECONDS);
    }

    public void cancelRunTimeoutTask() {
        if (runTimeoutTimer != null) {
            runTimeoutTimer.cancel();
        }
    }

    @Override
    public void run() {
        runnable.run();
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTraceId() {
        return traceId;
    }

}
