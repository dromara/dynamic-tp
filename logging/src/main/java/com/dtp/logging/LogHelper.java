package com.dtp.logging;

import org.slf4j.Logger;

/**
 * LogHelper related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public final class LogHelper {

    private static Logger monitorLogger;

    static {
        DtpLoggingInitializer.getInstance().loadConfiguration();
    }

    private LogHelper() { }

    public static void init(Logger logger) {
        monitorLogger = logger;
    }

    public static Logger getMonitorLogger() {
        return monitorLogger;
    }
}
