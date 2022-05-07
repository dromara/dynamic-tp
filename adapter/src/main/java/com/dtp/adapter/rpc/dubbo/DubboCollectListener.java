package com.dtp.adapter.rpc.dubbo;

import com.dtp.adapter.TpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.event.CollectEvent;
import com.dtp.core.handler.CollectorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * DubboCollectListener related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public class DubboCollectListener implements ApplicationListener<CollectEvent> {

    @Override
    public void onApplicationEvent(@NonNull CollectEvent event) {
        DtpProperties dtpProperties = event.getDtpProperties();
        try {
            TpHandler dubboTpHandler = ApplicationContextHolder.getBean(DubboTpHandler.class);
            Optional.ofNullable(dubboTpHandler.getMultiPoolStats())
                    .ifPresent(p -> p.forEach(f ->
                            CollectorHandler.getInstance().collect(f, dtpProperties.getCollectorType())));
        } catch (Exception e) {
            log.error("DynamicTp monitor, collect dubbo thread pool metrics failed.", e);
        }
    }
}