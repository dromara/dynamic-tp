package org.dromara.dynamictp.sdk.handler.refresher;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.spring.AbstractSpringRefresher;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

@Slf4j
public class AdminRefresher extends AbstractSpringRefresher implements SmartApplicationListener {

    protected AdminRefresher(DtpProperties dtpProperties) {
        super(dtpProperties);
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return false;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof AdminConfigEvent) {
            refresh(environment);
        }
    }
}
