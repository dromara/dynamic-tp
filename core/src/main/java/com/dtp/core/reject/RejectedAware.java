package com.dtp.core.reject;

import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.notify.AlarmManager;
import com.dtp.core.thread.DtpExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * RejectedAware related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public interface RejectedAware {

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
