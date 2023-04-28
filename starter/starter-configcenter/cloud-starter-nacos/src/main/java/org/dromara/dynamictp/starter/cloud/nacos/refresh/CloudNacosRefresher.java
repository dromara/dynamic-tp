package org.dromara.dynamictp.starter.cloud.nacos.refresh;

import com.dtp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.refresher.AbstractRefresher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.lang.NonNull;

/**
 * CloudNacosRefresher related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class CloudNacosRefresher extends AbstractRefresher implements SmartApplicationListener {

    public CloudNacosRefresher(DtpProperties dtpProperties) {
        super(dtpProperties);
    }

    @Override
    public boolean supportsEventType(@NonNull Class<? extends ApplicationEvent> eventType) {
        return RefreshScopeRefreshedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        if (event instanceof RefreshScopeRefreshedEvent) {
            doRefresh(dtpProperties);
        }
    }
}
