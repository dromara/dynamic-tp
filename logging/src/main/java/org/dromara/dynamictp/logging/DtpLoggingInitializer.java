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

import org.dromara.dynamictp.logging.log4j2.DtpLog4j2Logging;
import org.dromara.dynamictp.logging.logback.DtpLogbackLogging;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * DtpLogging related
 *
 * @author yanhom
 * @since 1.0.5
 **/
@Slf4j
public class DtpLoggingInitializer {

    private static AbstractDtpLogging dtpLogging;

    static  {
        try {
            Class.forName("ch.qos.logback.classic.Logger");
            dtpLogging = new DtpLogbackLogging();
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("org.apache.logging.log4j.LogManager");
                dtpLogging = new DtpLog4j2Logging();
            } catch (ClassNotFoundException classNotFoundException) {
                log.error("DynamicTp initialize logging failed, please check whether logback or log4j related dependencies exist.");
            }
        }
    }

    private static class LoggingInstance {
        private static final DtpLoggingInitializer INSTANCE = new DtpLoggingInitializer();
    }

    public static DtpLoggingInitializer getInstance() {
        return LoggingInstance.INSTANCE;
    }

    public void loadConfiguration() {
        if (Objects.isNull(dtpLogging)) {
            return;
        }
        dtpLogging.loadConfiguration();
        dtpLogging.initMonitorLogger();
    }
}
