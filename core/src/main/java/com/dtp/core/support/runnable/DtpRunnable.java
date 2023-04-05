package com.dtp.core.support.runnable;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.timer.HashedWheelTimer;
import com.dtp.common.timer.Timeout;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.timer.QueueTimeoutTimerTask;
import com.dtp.core.timer.RunTimeoutTimerTask;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;

import static com.dtp.common.constant.DynamicTpConst.TRACE_ID;

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
