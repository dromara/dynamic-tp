package com.dtp.logging;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * AbstractDtpLogging related
 *
 * @author yanhom
 * @since 1.0.5
 */
public abstract class AbstractDtpLogging {

    protected static final String MONITOR_LOG_NAME = "DTP.MONITOR.LOG";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String LOGGING_PATH = "LOG.PATH";
    private static final String APP_NAME = "APP.NAME";

    static {
        DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        String logPath = dtpProperties.getLogPath();
        if (StringUtils.isBlank(logPath)) {
            String userHome = System.getProperty("user.home");
            System.setProperty(LOGGING_PATH, userHome + File.separator + "logs");
        } else {
            System.setProperty(LOGGING_PATH, logPath);
        }

        String appName = ApplicationContextHolder.getEnvironment().getProperty("spring.application.name");
        appName = StringUtils.isNotBlank(appName) ? appName : "application";
        System.setProperty(APP_NAME, appName);
    }

    public URL getResourceUrl(String resource) throws IOException {

        if (resource.startsWith(CLASSPATH_PREFIX)) {
            String path = resource.substring(CLASSPATH_PREFIX.length());
            ClassLoader classLoader = DtpLoggingInitializer.class.getClassLoader();
            URL url = (classLoader != null ? classLoader.getResource(path) : ClassLoader.getSystemResource(path));
            if (url == null) {
                throw new FileNotFoundException("Cannot find file: +" + resource);
            }
            return url;
        }

        try {
            return new URL(resource);
        } catch (MalformedURLException ex) {
            return new File(resource).toURI().toURL();
        }
    }

    /**
     * Load configuration.
     */
    public abstract void loadConfiguration();

    /**
     * Init monitor logger.
     */
    public abstract void initMonitorLogger();
}
