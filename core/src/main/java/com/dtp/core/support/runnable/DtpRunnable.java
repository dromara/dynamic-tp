package com.dtp.core.support.runnable;

import com.dtp.common.timer.HashedWheelTimer;
import com.dtp.common.timer.Timeout;
import com.dtp.common.timer.TimerTask;
import com.dtp.common.util.TimeUtil;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static com.dtp.common.constant.DynamicTpConst.TRACE_ID;
import static com.dtp.common.em.NotifyItemEnum.RUN_TIMEOUT;

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

    private Timeout timeoutCheckTask;

    private static final HashedWheelTimer TIME_OUT_TIMER = new HashedWheelTimer();

    public DtpRunnable(Runnable runnable, String taskName) {
        this.runnable = runnable;
        submitTime = TimeUtil.currentTimeMillis();
        this.taskName = taskName;
        this.traceId = MDC.get(TRACE_ID);
    }

    public static void timeoutCheck(DtpExecutor executor,
                                    DtpRunnable runnable,
                                    long runTimeout,
                                    LongAdder runTimeoutCount) {
        if (runTimeout <= 0) {
            return;
        }
        TimeoutCheckTask task = new TimeoutCheckTask(executor, runnable, runTimeout, runTimeoutCount);
        runnable.timeoutCheckTask = TIME_OUT_TIMER.newTimeout(task, runTimeout, TimeUnit.MILLISECONDS);
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

    public Timeout getTimeoutCheckTask() {
        return timeoutCheckTask;
    }

    @Slf4j
    private static class TimeoutCheckTask implements TimerTask {

        private final DtpExecutor dtpExecutor;

        private final DtpRunnable runnable;

        private final long runTimeout;

        private final LongAdder runTimeoutCount;

        TimeoutCheckTask(DtpExecutor dtpExecutor,
                         DtpRunnable runnable,
                         long runTimeout,
                         LongAdder runTimeoutCount) {
            this.dtpExecutor = dtpExecutor;
            this.runnable = runnable;
            this.runTimeout = runTimeout;
            this.runTimeoutCount = runTimeoutCount;
        }

        @Override
        public void run(Timeout timeout) {
            runTimeoutCount.increment();
            AlarmManager.doAlarmAsync(dtpExecutor, RUN_TIMEOUT, runnable);
            if (StringUtils.isNotBlank(runnable.getTaskName()) || StringUtils.isNotBlank(runnable.getTraceId())) {
                log.warn("DynamicTp execute, run timeout, tpName: {}, taskName: {}, traceId: {}, runTime: {}ms",
                        dtpExecutor.getThreadPoolName(), runnable.getTaskName(), runnable.getTraceId(), runTimeout);
            }
        }

    }

}
