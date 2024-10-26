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

package org.dromara.dynamictp.starter.adapter.webserver;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.converter.ExecutorConverter;
import org.dromara.dynamictp.spring.holder.SpringContextHolder;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.Executor;

/**
 * AbstractWebServerDtpAdapter related
 *
 * @author yanhom
 * @author dragon-zhang
 * @since 1.0.0
 */@Slf4j
public abstract class AbstractWebServerDtpAdapter<A extends Executor> extends AbstractDtpAdapter
        implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof WebServerInitializedEvent) {
            try {
                DtpProperties dtpProperties = ContextManagerHelper.getBean(DtpProperties.class);
                initialize();
                afterInitialize();
                refresh(dtpProperties);
            } catch (Exception e) {
                log.error("Init web server thread pool failed.", e);
            }
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        if (executors.get(getTpName()) == null) {
            ApplicationContext applicationContext = SpringContextHolder.getInstance();
            WebServer webServer = ((WebServerApplicationContext) applicationContext).getWebServer();
            doEnhance(webServer);
            log.info("DynamicTp adapter, web server {} executor init end, executor: {}",
                    getTpName(), ExecutorConverter.toMainFields(executors.get(getTpName())));
        }
    }

    protected String getTpName() {
        return getTpPrefix();
    }

    /**
     * Do enhance.
     *
     * @param webServer webServer
     */
    protected abstract void doEnhance(WebServer webServer);
}
