package io.lyh.dtp.util;

import io.lyh.dtp.logging.DtpLogging;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * LogUtil related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2022-01-06 18:44
 * @since 1.0.0
 **/
public class LogUtil {
    
    public static final Logger MONITOR_LOGGER;
    private static final String MONITOR_LOG_NAME = "dtp.monitor.log";

    static {
        DtpLogging.getInstance().loadConfiguration();
        MONITOR_LOGGER = getLogger(MONITOR_LOG_NAME);
    }
    
    public static Logger logger(Class<?> clazz) {
        return getLogger(clazz);
    }
    
}
