package com.dtp.core.support.runnable;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.timer.HashedWheelTimer;
import com.dtp.common.timer.Timeout;
import com.dtp.common.timer.TimerTask;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    private Timeout runTimeoutCheckTask;

    private Timeout queueTimeoutCheckTask;

    public DtpRunnable(Runnable runnable, String taskName) {
        this.runnable = runnable;
        this.taskName = taskName;
        this.traceId = MDC.get(TRACE_ID);
    }

    public static void timeoutCheck(DtpExecutor executor,
                                    DtpRunnable runnable,
                                    NotifyItemEnum notifyItemEnum,
                                    long timeout,
                                    LongAdder timeoutCount) {
        if (timeout <= 0) {
            return;
        }
        TimeoutCheckTask task = new TimeoutCheckTask(executor, runnable, notifyItemEnum, timeout, timeoutCount);
        HashedWheelTimer hashedWheelTimer = ApplicationContextHolder.getBean(HashedWheelTimer.class);
        switch (notifyItemEnum) {
            case RUN_TIMEOUT:
                runnable.runTimeoutCheckTask = hashedWheelTimer.newTimeout(task, timeout, TimeUnit.MILLISECONDS);
                break;
            case QUEUE_TIMEOUT:
                runnable.queueTimeoutCheckTask = hashedWheelTimer.newTimeout(task, timeout, TimeUnit.MILLISECONDS);
                break;
            default:
                break;
        }
    }

    public static void cancelTimeoutCheckTask(DtpRunnable runnable, NotifyItemEnum notifyItemEnum) {
        switch (notifyItemEnum) {
            case RUN_TIMEOUT:
                if (runnable.runTimeoutCheckTask != null) {
                    runnable.runTimeoutCheckTask.cancel();
                }
                break;
            case QUEUE_TIMEOUT:
                if (runnable.queueTimeoutCheckTask != null) {
                    runnable.queueTimeoutCheckTask.cancel();
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

    @Slf4j
    private static class TimeoutCheckTask implements TimerTask {

        private final DtpExecutor dtpExecutor;

        private final DtpRunnable runnable;

        private final NotifyItemEnum notifyItemEnum;

        private final long timeout;

        private final LongAdder timeoutCount;

        TimeoutCheckTask(DtpExecutor dtpExecutor,
                         DtpRunnable runnable,
                         NotifyItemEnum notifyItemEnum,
                         long timeout,
                         LongAdder timeoutCount) {
            this.dtpExecutor = dtpExecutor;
            this.runnable = runnable;
            this.notifyItemEnum = notifyItemEnum;
            this.timeout = timeout;
            this.timeoutCount = timeoutCount;
        }

        @Override
        public void run(Timeout timeout) {
            timeoutCount.increment();
            AlarmManager.doAlarmAsync(dtpExecutor, notifyItemEnum);
            if (StringUtils.isNotBlank(runnable.getTaskName()) || StringUtils.isNotBlank(runnable.getTraceId())) {
                log.warn("DynamicTp execute, " + notifyItemEnum.name().toLowerCase() + " timeout, tpName: {}, taskName: {}, traceId: {}, runTime: {}ms",
                        dtpExecutor.getThreadPoolName(), runnable.getTaskName(), runnable.getTraceId(), this.timeout);
            }
        }

    }

}
