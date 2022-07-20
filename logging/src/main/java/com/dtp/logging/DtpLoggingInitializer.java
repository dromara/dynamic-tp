package com.dtp.logging;

import com.dtp.logging.log4j2.DtpLog4j2Logging;
import com.dtp.logging.logback.DtpLogbackLogging;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * DtpLogging related
 *
 * @author: yanhom
 * @since 1.0.5
 **/
@Slf4j
public class DtpLoggingInitializer {

    private static AbstractDtpLogging dtpLogging;

    static  {
        try {
            Class.forName("ch.qos.logback.classic.Logger");
            dtpLogging = new DtpLogbackLogging();
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("org.apache.logging.log4j.LogManager");
                dtpLogging = new DtpLog4j2Logging();
            } catch (ClassNotFoundException classNotFoundException) {
                log.error("DynamicTp initialize logging failed, " +
                        "please check whether logback or log4j related dependencies exist.");
            }
        }
    }

    private static class LoggingInstance {
        private static final DtpLoggingInitializer INSTANCE = new DtpLoggingInitializer();
    }

    public static DtpLoggingInitializer getInstance() {
        return LoggingInstance.INSTANCE;
    }

    public void loadConfiguration() {
        if (Objects.isNull(dtpLogging)) {
            return;
        }
        dtpLogging.loadConfiguration();
        dtpLogging.initMonitorLogger();
    }
}
