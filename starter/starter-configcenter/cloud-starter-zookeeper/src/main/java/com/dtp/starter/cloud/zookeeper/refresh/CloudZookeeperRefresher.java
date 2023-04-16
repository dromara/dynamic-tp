package com.dtp.starter.cloud.zookeeper.refresh;

import com.dtp.common.properties.DtpProperties;
import com.dtp.core.refresher.AbstractRefresher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.lang.NonNull;

/**
 * @author Redick01
 */
@Slf4j
public class CloudZookeeperRefresher extends AbstractRefresher implements SmartApplicationListener {

    public CloudZookeeperRefresher(DtpProperties dtpProperties) {
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
