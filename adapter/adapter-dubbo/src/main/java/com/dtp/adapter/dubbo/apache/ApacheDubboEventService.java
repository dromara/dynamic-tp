package com.dtp.adapter.dubbo.apache;

import com.dtp.adapter.common.AbstractDtpHandleListener;
import com.dtp.adapter.common.DtpHandler;
import com.dtp.adapter.dubbo.apache.handler.ApacheDubboDtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.core.handler.CollectorHandler;
import com.dtp.core.notify.AlarmManager;
import lombok.val;

import static com.dtp.common.constant.DynamicTpConst.SCHEDULE_ALARM_TYPES;
import static com.dtp.core.notify.AlarmManager.doAlarm;

/**
 * ApacheDubboEventService related
 *
 * @author yanhom
 * @since 1.0.6
 */
@SuppressWarnings("all")
public class ApacheDubboEventService extends AbstractDtpHandleListener {

    @Override
    protected void doCollect(DtpProperties dtpProperties) {
        DtpHandler apacheDubboDtpHandler = ApplicationContextHolder.getBean(ApacheDubboDtpHandler.class);
        apacheDubboDtpHandler.getMultiPoolStats().forEach(ps ->
                CollectorHandler.getInstance().collect(ps, dtpProperties.getCollectorType()));
    }

    @Override
    protected void doRefresh(DtpProperties dtpProperties) {
        DtpHandler apacheDubboDtpHandler = ApplicationContextHolder.getBean(ApacheDubboDtpHandler.class);
        apacheDubboDtpHandler.refresh(dtpProperties);
    }

    @Override
    protected void doAlarmCheck(DtpProperties dtpProperties) {
        DtpHandler apacheDubboDtpHandler = ApplicationContextHolder.getBean(ApacheDubboDtpHandler.class);
        val executorWrapper = apacheDubboDtpHandler.getExecutorWrappers();
        executorWrapper.forEach((k, v) -> AlarmManager.triggerAlarm(() -> doAlarm(v, SCHEDULE_ALARM_TYPES)));
    }
}
