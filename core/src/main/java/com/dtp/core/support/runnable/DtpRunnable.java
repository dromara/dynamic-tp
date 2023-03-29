package com.dtp.core.support.runnable;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.timer.HashedWheelTimer;
import com.dtp.common.timer.Timeout;
import com.dtp.common.timer.TimerTask;
import com.dtp.common.util.TimeUtil;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.thread.NamedThreadFactory;
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

    private final Long submitTime;

    private final String taskName;

    private final String traceId;

    private Timeout runTimeoutCheckTask;

    private Timeout queueTimeoutCheckTask;

    private static final HashedWheelTimer TIME_OUT_TIMER = new HashedWheelTimer(new NamedThreadFactory("dtpRunnable-timeout", true));

    public DtpRunnable(Runnable runnable, String taskName) {
        this.runnable = runnable;
        submitTime = TimeUtil.currentTimeMillis();
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
        switch (notifyItemEnum) {
            case RUN_TIMEOUT:
                runnable.runTimeoutCheckTask = TIME_OUT_TIMER.newTimeout(task, timeout, TimeUnit.MILLISECONDS);
                break;
            case QUEUE_TIMEOUT:
                runnable.queueTimeoutCheckTask = TIME_OUT_TIMER.newTimeout(task, timeout, TimeUnit.MILLISECONDS);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        runnable.run();
    }

    public Long getSubmitTime() {
        return submitTime;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTraceId() {
        return traceId;
    }

    public Timeout getRunTimeoutCheckTask() {
        return runTimeoutCheckTask;
    }

    public Timeout getQueueTimeoutCheckTask() {
        return queueTimeoutCheckTask;
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
