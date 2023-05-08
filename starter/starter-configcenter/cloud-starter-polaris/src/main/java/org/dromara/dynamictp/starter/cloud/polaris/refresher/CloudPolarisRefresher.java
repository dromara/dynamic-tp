package org.dromara.dynamictp.starter.cloud.polaris.refresher;

import org.dromara.dynamictp.core.refresher.AbstractRefresher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.lang.NonNull;

/**
 * CloudPolarisRefresher related
 *
 * @author fabian4
 * @since 1.0.0
 **/
@Slf4j
public class CloudPolarisRefresher extends AbstractRefresher implements SmartApplicationListener {

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
