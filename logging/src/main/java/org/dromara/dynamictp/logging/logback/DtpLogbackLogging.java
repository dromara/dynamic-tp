package org.dromara.dynamictp.logging.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import org.dromara.dynamictp.logging.AbstractDtpLogging;
import org.dromara.dynamictp.logging.LogHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * DtpLogbackLogging related
 *
 * @author yanhom
 * @since 1.0.5
 */
@Slf4j
public class DtpLogbackLogging extends AbstractDtpLogging {

    private static final String LOGBACK_LOCATION = "classpath:dtp-logback.xml";

    private LoggerContext loggerContext;

    @Override
    public void loadConfiguration() {
        try {
            loggerContext = new LoggerContext();
            new ContextInitializer(loggerContext).configureByResource(getResourceUrl(LOGBACK_LOCATION));
        } catch (Exception e) {
            log.error("Cannot initialize dtp logback logging.");
        }
    }

    public LoggerContext getLoggerContext() {
        return loggerContext;
    }

    @Override
    public void initMonitorLogger() {
        LogHelper.init(getLoggerContext().getLogger(MONITOR_LOG_NAME));
    }
}
