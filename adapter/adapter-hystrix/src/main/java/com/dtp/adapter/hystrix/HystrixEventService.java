package com.dtp.adapter.hystrix;

import com.dtp.adapter.common.AbstractDtpHandleListener;
import com.dtp.adapter.common.DtpHandler;
import com.dtp.adapter.hystrix.handler.HystrixDtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.core.handler.CollectorHandler;
import com.dtp.core.notify.AlarmManager;
import lombok.val;

import static com.dtp.common.constant.DynamicTpConst.SCHEDULE_ALARM_TYPES;
import static com.dtp.core.notify.AlarmManager.doAlarm;

/**
 * HystrixEventService related
 *
 * @author yanhom
 * @since 1.0.6
 */
public class HystrixEventService extends AbstractDtpHandleListener {

    @Override
    protected void doCollect(DtpProperties dtpProperties) {
        DtpHandler hystrixTpHandler = ApplicationContextHolder.getBean(HystrixDtpHandler.class);
        hystrixTpHandler.getMultiPoolStats().forEach(ps ->
                CollectorHandler.getInstance().collect(ps, dtpProperties.getCollectorType()));
    }

    @Override
    protected void doRefresh(DtpProperties dtpProperties) {
        DtpHandler hystrixTpHandler = ApplicationContextHolder.getBean(HystrixDtpHandler.class);
        hystrixTpHandler.refresh(dtpProperties);
    }

    @Override
    protected void doAlarmCheck(DtpProperties dtpProperties) {
        DtpHandler hystrixDtpHandler = ApplicationContextHolder.getBean(HystrixDtpHandler.class);
        val executorWrapper = hystrixDtpHandler.getExecutorWrappers();
        executorWrapper.forEach((k, v) -> AlarmManager.triggerAlarm(() -> doAlarm(v, SCHEDULE_ALARM_TYPES)));
    }
}
