package com.dtp.core.support.runnable;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.timer.HashedWheelTimer;
import com.dtp.common.timer.Timeout;
import com.dtp.core.timer.QueueTimeoutTimerTask;
import com.dtp.core.timer.RunTimeoutTimerTask;
import com.dtp.core.thread.DtpExecutor;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

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

    public void startTimeoutTask(DtpExecutor executor,
                                 NotifyItemEnum notifyItemEnum,
                                 long timeout,
                                 LongAdder timeoutCount) {
        if (timeout <= 0) {
            return;
        }
        HashedWheelTimer hashedWheelTimer = ApplicationContextHolder.getBean(HashedWheelTimer.class);
        switch (notifyItemEnum) {
            case RUN_TIMEOUT:
                RunTimeoutTimerTask runTimeoutTimerTask = new RunTimeoutTimerTask(executor, this, timeoutCount);
                runTimeoutTimer = hashedWheelTimer.newTimeout(runTimeoutTimerTask, timeout, TimeUnit.MILLISECONDS);
                break;
            case QUEUE_TIMEOUT:
                QueueTimeoutTimerTask queueTimeoutTimerTask = new QueueTimeoutTimerTask(executor, this, timeoutCount);
                queueTimeoutTimer = hashedWheelTimer.newTimeout(queueTimeoutTimerTask, timeout, TimeUnit.MILLISECONDS);
                break;
            default:
                break;
        }
    }

    public void cancelTimeoutCheckTask(NotifyItemEnum notifyItemEnum) {
        switch (notifyItemEnum) {
            case RUN_TIMEOUT:
                if (runTimeoutTimer != null) {
                    runTimeoutTimer.cancel();
                }
                break;
            case QUEUE_TIMEOUT:
                if (queueTimeoutTimer != null) {
                    queueTimeoutTimer.cancel();
                }
                break;
            default:
                break;
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
