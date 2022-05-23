package com.dtp.adapter.dubbo.hystrix;

import com.dtp.adapter.dubbo.common.DtpHandleListener;
import com.dtp.adapter.dubbo.common.DtpHandler;
import com.dtp.adapter.dubbo.hystrix.handler.HystrixDtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.core.handler.CollectorHandler;

/**
 * HystrixEventService related
 *
 * @author yanhom
 * @since 1.0.6
 */
public class HystrixEventService extends DtpHandleListener {

    @Override
    protected void doCollect(DtpProperties dtpProperties) {
        DtpHandler hystrixTpHandler = ApplicationContextHolder.getBean(HystrixDtpHandler.class);
        hystrixTpHandler.getMultiPoolStats().forEach(ps ->
                CollectorHandler.getInstance().collect(ps, dtpProperties.getCollectorType()));
    }

    @Override
    protected void doUpdate(DtpProperties dtpProperties) {
        DtpHandler hystrixTpHandler = ApplicationContextHolder.getBean(HystrixDtpHandler.class);
        hystrixTpHandler.updateTp(dtpProperties);
    }
}
