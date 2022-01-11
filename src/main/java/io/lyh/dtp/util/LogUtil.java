package io.lyh.dtp.util;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * LogUtil related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class LogUtil {

    public static final Logger MONITOR_LOGGER;
    private static final String MONITOR_LOG_NAME = "dtp.monitor.log";

    static {
        MONITOR_LOGGER = getLogger(MONITOR_LOG_NAME);
    }

    public static Logger logger(Class<?> clazz) {
        return getLogger(clazz);
    }
}
