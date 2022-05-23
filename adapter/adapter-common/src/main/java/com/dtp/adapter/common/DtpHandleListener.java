package com.dtp.adapter.common;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.event.CollectEvent;
import com.dtp.common.event.RefreshEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;

/**
 * DtpHandleListener related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public abstract class DtpHandleListener implements GenericApplicationListener {

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        Class<?> type = resolvableType.getRawClass();
        if (type != null) {
            return RefreshEvent.class.isAssignableFrom(type) || CollectEvent.class.isAssignableFrom(type);
        }
        return false;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        try {
            if (event instanceof RefreshEvent) {
                doUpdate(((RefreshEvent) event).getDtpProperties());
            } else if (event instanceof CollectEvent) {
                doCollect(((CollectEvent) event).getDtpProperties());
            }
        } catch (Exception e) {
            log.error("DynamicTp adapter, event handle failed.", e);
        }
    }

    /**
     * Do collect thread pool stats.
     * @param dtpProperties dtpProperties
     */
    protected abstract void doCollect(DtpProperties dtpProperties);

    /**
     * Do update.
     * @param dtpProperties dtpProperties
     */
    protected abstract void doUpdate(DtpProperties dtpProperties);
}