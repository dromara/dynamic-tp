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

package org.dromara.dynamictp.starter.adapter.webserver.undertow;

import io.undertow.Undertow;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.starter.adapter.webserver.AbstractWebServerDtpAdapter;
import org.dromara.dynamictp.starter.adapter.webserver.undertow.taskpool.EnhancedQueueExecutorTaskPoolAdapter.EnhancedQueueExecutorAdapter;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.boot.web.server.WebServer;
import org.xnio.XnioWorker;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * UndertowDtpAdapter related
 *
 * @author yanhom
 * @author dragon-zhang
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings("all")
public class UndertowDtpAdapter extends AbstractWebServerDtpAdapter<XnioWorker> {

    private static final String TP_PREFIX = "undertowTp";

    public UndertowDtpAdapter() {
        super();
        System.setProperty("jboss.threads.eqe.statistics.active-count", "true");
        System.setProperty("jboss.threads.eqe.statistics", "true");
    }

    @Override
    public void doEnhance(WebServer webServer) {
        val undertowServletWebServer = (UndertowServletWebServer) webServer;
        val undertow = (Undertow) ReflectionUtil.getFieldValue(UndertowServletWebServer.class,
                "undertow", undertowServletWebServer);
        if (Objects.isNull(undertow)) {
            return;
        }
        XnioWorker xnioWorker = undertow.getWorker();
        Object taskPool = ReflectionUtil.getFieldValue(XnioWorker.class, "taskPool", xnioWorker);
        if (Objects.isNull(taskPool)) {
            return;
        }
        val handler = TaskPoolHandlerFactory.getTaskPoolHandler(taskPool.getClass().getSimpleName());
        String internalExecutor = handler.taskPoolType().getInternalExecutor();
        Object executor = ReflectionUtil.getFieldValue(taskPool.getClass(), internalExecutor, taskPool);
        String tpName = getTpName();
        if (executor instanceof ThreadPoolExecutor) {
            enhanceOriginExecutor(tpName, (ThreadPoolExecutor) executor, internalExecutor, taskPool);
        } else if (executor instanceof EnhancedQueueExecutor) {
            try {
                val proxy = new EnhancedQueueExecutorProxy((EnhancedQueueExecutor) executor);
                ReflectionUtil.setFieldValue(internalExecutor, taskPool, proxy);
                putAndFinalize(tpName, (ExecutorService) executor, new EnhancedQueueExecutorAdapter(proxy));
            } catch (Throwable t) {
                log.error("DynamicTp adapter, enhance {} failed, please adjust the order of the two dependencies" +
                        "(spring-boot-starter-undertow and starter-adapter-webserver) and try again.", tpName, t);
                executors.put(tpName, new ExecutorWrapper(tpName, handler.adapt(executor)));
            }
        } else {
            executors.put(tpName, new ExecutorWrapper(tpName, handler.adapt(executor)));
        }
    }

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(executors.get(getTpName()), dtpProperties.getPlatforms(), dtpProperties.getUndertowTp());
    }

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }
}
