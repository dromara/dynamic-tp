package io.lyh.dynamic.tp.core.reject;

import io.lyh.dynamic.tp.common.em.NotifyTypeEnum;
import io.lyh.dynamic.tp.core.notify.AlarmManager;
import io.lyh.dynamic.tp.core.DtpExecutor;

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
     * @param executor
     */
    default void beforeReject(ThreadPoolExecutor executor) {
        if (executor instanceof DtpExecutor) {
            DtpExecutor dtpExecutor = (DtpExecutor) executor;
            dtpExecutor.incRejectCount(1);
            AlarmManager.triggerAlarm(() -> AlarmManager.doAlarm(dtpExecutor, NotifyTypeEnum.REJECT));
        }
    }

}
