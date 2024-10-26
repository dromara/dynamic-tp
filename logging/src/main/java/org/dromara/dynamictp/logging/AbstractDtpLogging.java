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

package org.dromara.dynamictp.logging;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.properties.DtpProperties;

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
@Slf4j
public abstract class AbstractDtpLogging {

    protected static final String MONITOR_LOG_NAME = "DTP.MONITOR.LOG";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String LOGGING_PATH = "LOG.PATH";

    static {
        try {
            DtpProperties dtpProperties = ContextManagerHelper.getBean(DtpProperties.class);
            String logPath = dtpProperties.getLogPath();
            if (StringUtils.isBlank(logPath)) {
                String userHome = System.getProperty("user.home");
                System.setProperty(LOGGING_PATH, userHome + File.separator + "logs");
            } else {
                System.setProperty(LOGGING_PATH, logPath);
            }
        } catch (Exception e) {
            log.error("DynamicTp logging env init failed, if collectType is not logging, this error can be ignored.", e);
        }
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
