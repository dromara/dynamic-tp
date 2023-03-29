package com.dtp.core.timer;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.timer.Timeout;
import com.dtp.common.timer.TimerTask;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.support.runnable.DtpRunnable;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.LongAdder;

@Slf4j
public class QueueTimeoutTimerTask implements TimerTask {

    private final DtpExecutor dtpExecutor;

    private final DtpRunnable runnable;

    private final LongAdder timeoutCount;

    public QueueTimeoutTimerTask(DtpExecutor dtpExecutor,
                                 DtpRunnable runnable,
                                 LongAdder timeoutCount) {
        this.dtpExecutor = dtpExecutor;
        this.runnable = runnable;
        this.timeoutCount = timeoutCount;
    }

    @Override
    public void run(Timeout timeout) {
        timeoutCount.increment();
        AlarmManager.doAlarmAsync(dtpExecutor, NotifyItemEnum.QUEUE_TIMEOUT);
        if (StringUtils.isNotBlank(runnable.getTaskName()) || StringUtils.isNotBlank(runnable.getTraceId())) {
            log.warn("DynamicTp execute, queue timeout, tpName: {}, taskName: {}, traceId: {}",
                    dtpExecutor.getThreadPoolName(), runnable.getTaskName(), runnable.getTraceId());
        }
    }

}
