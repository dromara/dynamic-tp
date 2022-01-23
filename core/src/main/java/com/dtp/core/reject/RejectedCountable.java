package com.dtp.core.reject;

import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.notify.AlarmManager;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * RejectedCountable related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public interface RejectedCountable extends RejectedExecutionHandler {

    /**
     * Do sth before reject.
     * @param executor ThreadPoolExecutor instance
     */
    default void beforeReject(ThreadPoolExecutor executor) {
        if (executor instanceof DtpExecutor) {
            DtpExecutor dtpExecutor = (DtpExecutor) executor;
            dtpExecutor.incRejectCount(1);
            AlarmManager.triggerAlarm(() -> AlarmManager.doAlarm(dtpExecutor, NotifyTypeEnum.REJECT));
        }
    }

}
