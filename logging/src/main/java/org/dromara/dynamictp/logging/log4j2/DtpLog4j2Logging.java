/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.logging.log4j2;

import org.dromara.dynamictp.logging.AbstractDtpLogging;
import org.dromara.dynamictp.logging.LogHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.net.URL;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * DtpLog4j2Logging related
 *
 * @author yanhom
 * @since 1.0.5
 */
@Slf4j
public class DtpLog4j2Logging extends AbstractDtpLogging {

    private static final String LOG4J2_LOCATION = "classpath:dtp-log4j2.xml";
    private static final String LOGGER_NAME_PREFIX = "DTP";

    @Override
    public void loadConfiguration() {

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = loadConfiguration(loggerContext, LOG4J2_LOCATION);
        if (configuration == null) {
            return;
        }

        configuration.start();
        Map<String, Appender> appenderMap = configuration.getAppenders();
        Configuration contextConfiguration = loggerContext.getConfiguration();
        for (Appender appender : appenderMap.values()) {
            contextConfiguration.addAppender(appender);
        }
        Map<String, LoggerConfig> loggers = configuration.getLoggers();
        loggers.forEach((k, v) -> {
            if (k.startsWith(LOGGER_NAME_PREFIX)) {
                contextConfiguration.addLogger(k, v);
            }
        });

        loggerContext.updateLoggers();
    }

    private Configuration loadConfiguration(LoggerContext loggerContext, String location) {
        try {
            URL url = getResourceUrl(location);
            ConfigurationSource source = new ConfigurationSource(url.openStream(), url);
            return ConfigurationFactory.getInstance().getConfiguration(loggerContext, source);
        } catch (Exception e) {
            log.error("Cannot initialize dtp log4j2 logging.");
            return null;
        }
    }

    @Override
    public void initMonitorLogger() {
        LogHelper.init(getLogger(MONITOR_LOG_NAME));
    }
}
