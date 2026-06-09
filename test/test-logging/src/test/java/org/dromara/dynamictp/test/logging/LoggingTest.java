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

package org.dromara.dynamictp.test.logging;

import org.dromara.dynamictp.logging.AbstractDtpLogging;
import org.dromara.dynamictp.logging.DtpLoggingInitializer;
import org.dromara.dynamictp.logging.LogHelper;
import org.dromara.dynamictp.logging.logback.DtpLogbackLogging;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.spring.holder.SpringContextHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingTest {

    @TempDir
    Path tempDir;

    @BeforeAll
    static void setUpContext() {
        DtpProperties.getInstance().setLogPath("target/dtp-test-logs");
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(DtpProperties.class, DtpProperties::getInstance);
        context.refresh();
        new SpringContextHolder().setApplicationContext(context);
    }

    @Test
    void shouldResolveClasspathUrlAndFileResources() throws Exception {
        TestLogging logging = new TestLogging();
        Path config = Files.createFile(tempDir.resolve("dtp-test.xml"));

        assertEquals("file", logging.getResourceUrl("classpath:dtp-logback.xml").getProtocol());
        assertEquals("https", logging.getResourceUrl("https://dynamictp.cn").getProtocol());
        assertEquals(config.toUri().toURL(), logging.getResourceUrl(config.toString()));
    }

    @Test
    void shouldRejectMissingClasspathResource() {
        TestLogging logging = new TestLogging();

        assertThrows(FileNotFoundException.class,
                () -> logging.getResourceUrl("classpath:missing-dtp-log-config.xml"));
    }

    @Test
    void shouldInitializeLogbackMonitorLogger() {
        DtpLogbackLogging logging = new DtpLogbackLogging();

        logging.loadConfiguration();
        logging.initMonitorLogger();

        assertNotNull(logging.getLoggerContext());
        assertNotNull(LogHelper.getMonitorLogger());
        assertEquals("DTP.MONITOR.LOG", LogHelper.getMonitorLogger().getName());
    }

    @Test
    void shouldExposeSingletonAndAllowMonitorLoggerReplacement() {
        Logger logger = LoggerFactory.getLogger("test-monitor");

        assertSame(DtpLoggingInitializer.getInstance(), DtpLoggingInitializer.getInstance());
        LogHelper.init(logger);
        assertSame(logger, LogHelper.getMonitorLogger());
    }

    private static class TestLogging extends AbstractDtpLogging {

        private final AtomicBoolean configured = new AtomicBoolean();

        @Override
        public void loadConfiguration() {
            configured.set(true);
        }

        @Override
        public void initMonitorLogger() {
            assertTrue(configured.get());
        }
    }
}
