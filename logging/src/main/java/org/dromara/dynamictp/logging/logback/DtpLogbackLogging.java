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

package org.dromara.dynamictp.logging.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import org.dromara.dynamictp.logging.AbstractDtpLogging;
import org.dromara.dynamictp.logging.LogHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * DtpLogbackLogging related
 *
 * @author yanhom
 * @since 1.0.5
 */
@Slf4j
public class DtpLogbackLogging extends AbstractDtpLogging {

    private static final String LOGBACK_LOCATION = "classpath:dtp-logback.xml";

    private LoggerContext loggerContext;

    @Override
    public void loadConfiguration() {
        try {
            loggerContext = new LoggerContext();
            new ContextInitializer(loggerContext).configureByResource(getResourceUrl(LOGBACK_LOCATION));
        } catch (Exception e) {
            log.error("Cannot initialize dtp logback logging.");
        }
    }

    public LoggerContext getLoggerContext() {
        return loggerContext;
    }

    @Override
    public void initMonitorLogger() {
        LogHelper.init(getLoggerContext().getLogger(MONITOR_LOG_NAME));
    }
}
