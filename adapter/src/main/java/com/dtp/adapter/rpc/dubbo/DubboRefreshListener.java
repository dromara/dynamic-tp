package com.dtp.adapter.rpc.dubbo;

import com.dtp.adapter.TpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.event.RefreshEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

/**
 * DubboRefreshListener related
 *
 * @author: yanhom
 * @since 1.0.6
 **/
@Slf4j
public class DubboRefreshListener implements ApplicationListener<RefreshEvent> {

    @Override
    public void onApplicationEvent(@NonNull RefreshEvent event) {
        try {
            TpHandler dubboTpHandler = ApplicationContextHolder.getBean(DubboTpHandler.class);
            dubboTpHandler.updateTp(event.getDtpProperties());
        } catch (Exception e) {
            log.error("DynamicTp refresh, update dubbo thread pool failed.", e);
        }
    }
}