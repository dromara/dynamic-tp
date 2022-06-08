package com.dtp.adapter.rocketmq;

import com.dtp.adapter.common.DtpHandleListener;
import com.dtp.adapter.common.DtpHandler;
import com.dtp.adapter.rocketmq.handler.RocketMqDtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.core.handler.CollectorHandler;

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
}
