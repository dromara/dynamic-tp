package com.dtp.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;

import java.util.Set;

/**
 * Reload dtp log configuration file, after
 * {@link org.springframework.boot.context.logging.LoggingApplicationListener}.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpLoggingListener implements GenericApplicationListener {

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        Class<?> type = resolvableType.getRawClass();
        if (type != null) {
            return ApplicationStartedEvent.class.isAssignableFrom(type);
        }
        return false;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        try {
            Class.forName("org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration");
            Class<?> type = applicationEvent.getSource().getClass();
            if (SpringApplication.class.isAssignableFrom(type)) {
                SpringApplication application = (SpringApplication) applicationEvent.getSource();
                Set<Object> sources = application.getAllSources();
                if (sources.size() == 1 && sources.contains(BootstrapImportSelectorConfiguration.class)) {
                    return;
                }
            }
        } catch (ClassNotFoundException e) {
            log.warn("DynamicTp logging init, not spring cloud application.");
        }

        DtpLogging.getInstance().loadConfiguration();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 24;
    }

}
