package io.lyh.dtp.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * DtpLogging related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2022-01-06 18:44
 * @since 1.0.0
 **/
public class DtpLogging {

    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String LOGBACK_LOCATION = "classpath:dtp-logback.xml";
    private static final String LOGGING_PATH = "LOG.PATH";

    static {
        String logPath = System.getProperty(LOGGING_PATH);
        if (StringUtils.isBlank(logPath)) {
            String userHome = System.getProperty("user.home");
            System.setProperty(LOGGING_PATH, userHome + File.separator + "logs");
        }
    }

    private static class LoggingInstance {

        private static final DtpLogging INSTANCE = new DtpLogging();
    }

    public static DtpLogging getInstance() {
        return DtpLogging.LoggingInstance.INSTANCE;
    }

    public void loadConfiguration() {
        try {
            LoggerContext loggerContext = (LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory();
            new ContextInitializer(loggerContext).configureByResource(getResourceUrl(LOGBACK_LOCATION));
        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize logback from " + LOGBACK_LOCATION, e);
        }
    }

    public static URL getResourceUrl(String resource) throws IOException {

        if (resource.startsWith(CLASSPATH_PREFIX)) {
            String path = resource.substring(CLASSPATH_PREFIX.length());
            ClassLoader classLoader = DtpLogging.class.getClassLoader();
            URL url = (classLoader != null ? classLoader.getResource(path) : ClassLoader.getSystemResource(path));
            if (url == null) {
                throw new FileNotFoundException("Resource [" + resource + "] does not exist...");
            }
            return url;
        }

        try {
            return new URL(resource);
        } catch (MalformedURLException ex) {
            return new File(resource).toURI().toURL();
        }
    }
}
