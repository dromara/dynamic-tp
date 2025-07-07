package org.dromara.dynamictp.sdk.handler.refresher;

import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public abstract class AdminConfigEvent extends ApplicationEvent {
    public AdminConfigEvent(Object source) {
        super(source);
    }

    public AdminConfigEvent(Object source, Clock clock) {
        super(source, clock);
    }

}
