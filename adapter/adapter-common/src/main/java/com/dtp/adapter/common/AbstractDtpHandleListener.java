package com.dtp.adapter.common;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.event.AlarmCheckEvent;
import com.dtp.common.event.CollectEvent;
import com.dtp.common.event.RefreshEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;

/**
 * AbstractDtpHandleListener related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public abstract class AbstractDtpHandleListener implements GenericApplicationListener {

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        Class<?> type = resolvableType.getRawClass();
        if (type != null) {
            return RefreshEvent.class.isAssignableFrom(type) ||
                    CollectEvent.class.isAssignableFrom(type) ||
                    AlarmCheckEvent.class.isAssignableFrom(type);
        }
        return false;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        try {
            if (event instanceof RefreshEvent) {
                doRefresh(((RefreshEvent) event).getDtpProperties());
            } else if (event instanceof CollectEvent) {
                doCollect(((CollectEvent) event).getDtpProperties());
            } else if (event instanceof AlarmCheckEvent) {
                doAlarmCheck(((AlarmCheckEvent) event).getDtpProperties());
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
     * Do refresh.
     * @param dtpProperties dtpProperties
     */
    protected abstract void doRefresh(DtpProperties dtpProperties);

    /**
     * Do alarm check.
     * @param dtpProperties dtpProperties
     */
    protected void doAlarmCheck(DtpProperties dtpProperties) {}
}