package com.dtp.common.util;

import org.slf4j.Logger;

/**
 * LogUtil related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class LogUtil {

    private LogUtil() {}

    private static Logger monitorLogger = null;

    public static void init(Logger logger) {
        monitorLogger = logger;
    }

    public static Logger getMonitorLogger() {
        return monitorLogger;
    }
}
