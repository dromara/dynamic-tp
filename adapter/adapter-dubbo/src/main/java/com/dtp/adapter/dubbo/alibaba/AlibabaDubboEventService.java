package com.dtp.adapter.dubbo.alibaba;


import com.dtp.adapter.common.DtpHandleListener;
import com.dtp.adapter.common.DtpHandler;
import com.dtp.adapter.dubbo.alibaba.handler.AlibabaDubboDtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.core.handler.CollectorHandler;

/**
 * DubboEventService related
 *
 * @author yanhom
 * @since 1.0.6
 */
@SuppressWarnings("all")
public class AlibabaDubboEventService extends DtpHandleListener {

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
}
