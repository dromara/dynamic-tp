package com.dtp.common.util;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * LogUtil related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class LogUtil {

    private LogUtil() {}

    public static final Logger MONITOR_LOGGER;
    private static final String MONITOR_LOG_NAME = "DTP.MONITOR.LOG";

    static {
        MONITOR_LOGGER = getLogger(MONITOR_LOG_NAME);
    }

    public static Logger logger(Class<?> clazz) {
        return getLogger(clazz);
    }
}
