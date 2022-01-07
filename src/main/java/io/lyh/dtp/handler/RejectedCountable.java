package io.lyh.dtp.handler;

import io.lyh.dtp.common.em.NotifyTypeEnum;
import io.lyh.dtp.core.DtpExecutor;
import io.lyh.dtp.notify.AlarmManager;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * RejectedCountable related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-30 16:10
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
