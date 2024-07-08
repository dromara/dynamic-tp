package org.dromara.dynamictp.common.spring;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventPublisher {

    private final List<EventListener<? extends Event>> listeners = new CopyOnWriteArrayList<>();

    public <T extends Event> void publish(T event) {
        for (EventListener<? extends Event> listener : listeners) {
            if (listener.getClass().isAssignableFrom(event.getClass())) {
                ((EventListener<T>) listener).onEvent(event);
            }
        }
    }

    public void registerListener(EventListener<? extends Event> listener) {
        listeners.add(listener);
    }

    public void unregisterListener(EventListener<? extends Event> listener) {
        listeners.remove(listener);
    }
}