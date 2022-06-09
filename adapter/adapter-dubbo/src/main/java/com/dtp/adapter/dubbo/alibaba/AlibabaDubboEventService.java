package com.dtp.adapter.dubbo.alibaba;


import com.dtp.adapter.common.AbstractDtpHandleListener;
import com.dtp.adapter.common.DtpHandler;
import com.dtp.adapter.dubbo.alibaba.handler.AlibabaDubboDtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.core.handler.CollectorHandler;
import com.dtp.core.notify.AlarmManager;
import lombok.val;

import static com.dtp.common.constant.DynamicTpConst.SCHEDULE_ALARM_TYPES;
import static com.dtp.core.notify.AlarmManager.doAlarm;

/**
 * DubboEventService related
 *
 * @author yanhom
 * @since 1.0.6
 */
@SuppressWarnings("all")
public class AlibabaDubboEventService extends AbstractDtpHandleListener {

    @Override
    protected void doCollect(DtpProperties dtpProperties) {
        DtpHandler dubboTpHandler = ApplicationContextHolder.getBean(AlibabaDubboDtpHandler.class);
        dubboTpHandler.getMultiPoolStats().forEach(ps ->
                CollectorHandler.getInstance().collect(ps, dtpProperties.getCollectorType()));
    }

    @Override
    protected void doRefresh(DtpProperties dtpProperties) {
        DtpHandler dubboTpHandler = ApplicationContextHolder.getBean(AlibabaDubboDtpHandler.class);
        dubboTpHandler.refresh(dtpProperties);
    }

    @Override
    protected void doAlarmCheck(DtpProperties dtpProperties) {
        DtpHandler alibabaDubboDtpHandler = ApplicationContextHolder.getBean(AlibabaDubboDtpHandler.class);
        val executorWrapper = alibabaDubboDtpHandler.getExecutorWrappers();
        executorWrapper.forEach((k, v) -> AlarmManager.triggerAlarm(() -> doAlarm(v, SCHEDULE_ALARM_TYPES)));
    }
}
