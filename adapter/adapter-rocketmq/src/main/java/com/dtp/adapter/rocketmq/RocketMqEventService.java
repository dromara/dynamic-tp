package com.dtp.adapter.rocketmq;

import com.dtp.adapter.common.DtpHandleListener;
import com.dtp.adapter.common.DtpHandler;
import com.dtp.adapter.rocketmq.handler.RocketMqDtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.core.handler.CollectorHandler;
import com.dtp.core.notify.AlarmManager;
import lombok.val;

import static com.dtp.common.constant.DynamicTpConst.SCHEDULE_ALARM_TYPES;
import static com.dtp.core.notify.AlarmManager.doAlarm;

/**
 * RocketMqEventService related
 *
 * @author yanhom
 * @since 1.0.6
 */
public class RocketMqEventService extends DtpHandleListener {

    @Override
    protected void doCollect(DtpProperties dtpProperties) {
        DtpHandler rocketMqDtpHandler = ApplicationContextHolder.getBean(RocketMqDtpHandler.class);
        rocketMqDtpHandler.getMultiPoolStats().forEach(ps ->
                CollectorHandler.getInstance().collect(ps, dtpProperties.getCollectorType()));
    }

    @Override
    protected void doRefresh(DtpProperties dtpProperties) {
        DtpHandler rocketMqDtpHandler = ApplicationContextHolder.getBean(RocketMqDtpHandler.class);
        rocketMqDtpHandler.refresh(dtpProperties);
    }

    @Override
    protected void doAlarmCheck(DtpProperties dtpProperties) {
        DtpHandler rocketMqDtpHandler = ApplicationContextHolder.getBean(RocketMqDtpHandler.class);
        val executorWrapper = rocketMqDtpHandler.getExecutorWrappers();
        executorWrapper.forEach((k, v) -> AlarmManager.triggerAlarm(() -> doAlarm(v, SCHEDULE_ALARM_TYPES)));
    }
}
