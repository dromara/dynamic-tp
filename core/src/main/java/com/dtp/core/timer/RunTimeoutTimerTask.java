package com.dtp.core.timer;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.timer.Timeout;
import com.dtp.common.timer.TimerTask;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.support.runnable.DtpRunnable;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


@Slf4j
public class RunTimeoutTimerTask implements TimerTask {

    private final DtpExecutor dtpExecutor;

    private final DtpRunnable runnable;

    public RunTimeoutTimerTask(DtpExecutor dtpExecutor,
                               DtpRunnable runnable) {
        this.dtpExecutor = dtpExecutor;
        this.runnable = runnable;
    }

    @Override
    public void run(Timeout timeout) {
        dtpExecutor.getRunTimeoutCount().increment();
        AlarmManager.doAlarmAsync(dtpExecutor, NotifyItemEnum.RUN_TIMEOUT);
        if (StringUtils.isNotBlank(runnable.getTaskName()) || StringUtils.isNotBlank(runnable.getTraceId())) {
            log.warn("DynamicTp execute, run timeout, tpName: {}, taskName: {}, traceId: {}",
                    dtpExecutor.getThreadPoolName(), runnable.getTaskName(), runnable.getTraceId());
        }
    }

}
