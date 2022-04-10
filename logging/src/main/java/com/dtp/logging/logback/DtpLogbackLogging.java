package com.dtp.logging.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import com.dtp.common.util.LogUtil;
import com.dtp.logging.AbstractDtpLogging;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.impl.StaticLoggerBinder;

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
            loggerContext = (LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory();
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
        LogUtil.init(getLoggerContext().getLogger(MONITOR_LOG_NAME));
    }
}
