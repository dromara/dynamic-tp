package com.dtp.adapter.dubbo.apache;

import com.dtp.adapter.dubbo.apache.handler.ApacheDubboDtpHandler;
import com.dtp.adapter.dubbo.common.DtpHandleListener;
import com.dtp.adapter.dubbo.common.DtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.core.handler.CollectorHandler;

/**
 * ApacheDubboEventService related
 *
 * @author yanhom
 * @since 1.0.6
 */
@SuppressWarnings("all")
public class ApacheDubboEventService extends DtpHandleListener {

    @Override
    protected void doCollect(DtpProperties dtpProperties) {
        DtpHandler apacheDubboDtpHandler = ApplicationContextHolder.getBean(ApacheDubboDtpHandler.class);
        apacheDubboDtpHandler.getMultiPoolStats().forEach(ps ->
                CollectorHandler.getInstance().collect(ps, dtpProperties.getCollectorType()));
    }

    @Override
    protected void doUpdate(DtpProperties dtpProperties) {
        DtpHandler apacheDubboDtpHandler = ApplicationContextHolder.getBean(ApacheDubboDtpHandler.class);
        apacheDubboDtpHandler.updateTp(dtpProperties);
    }
}
