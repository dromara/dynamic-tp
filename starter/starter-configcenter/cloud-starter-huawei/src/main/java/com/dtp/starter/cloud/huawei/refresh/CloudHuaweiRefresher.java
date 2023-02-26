package com.dtp.starter.cloud.huawei.refresh;

import com.dtp.core.refresh.AbstractRefresher;
import com.huaweicloud.common.event.ConfigRefreshEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.lang.NonNull;

/**
 * @author Redick01
 */
@Slf4j
public class CloudHuaweiRefresher extends AbstractRefresher implements SmartApplicationListener {

    @Override
    public boolean supportsEventType(@NonNull Class<? extends ApplicationEvent> eventType) {
        return ConfigRefreshEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        // huawei config define RefreshEvent
        if (event instanceof ConfigRefreshEvent) {
            doRefresh(dtpProperties);
        }
    }
}
