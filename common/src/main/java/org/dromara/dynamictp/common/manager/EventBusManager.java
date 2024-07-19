package org.dromara.dynamictp.common.manager;

import com.google.common.eventbus.EventBus;

public class EventBusManager {

    private static final EventBus EVENT_BUS = new EventBus();

    private EventBusManager() { }

    public static void register(Object object) {
        EVENT_BUS.register(object);
    }

    public static void unregister(Object object) {
        EVENT_BUS.unregister(object);
    }

    public static void post(Object event) {
        EVENT_BUS.post(event);
    }

    public static EventBus getInstance() {
        return EVENT_BUS;
    }
}

