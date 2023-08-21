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

package org.dromara.dynamictp.starter.adapter.webserver.adapter.undertow;

import org.dromara.dynamictp.starter.adapter.webserver.adapter.AbstractWebServerDtpAdapter;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import io.undertow.Undertow;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dromara.dynamictp.core.support.ThreadPoolExecutorProxy;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.boot.web.server.WebServer;
import org.xnio.XnioWorker;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * UndertowDtpAdapter related
 *
 * @author yanhom
 * @author dragon-zhang
 * @since 1.0.0
 */
@Slf4j
public class UndertowDtpAdapter extends AbstractWebServerDtpAdapter<XnioWorker> {

    private static final String POOL_NAME = "undertowTp";

    public UndertowDtpAdapter() {
        super();
        System.setProperty("jboss.threads.eqe.statistics.active-count", "true");
        System.setProperty("jboss.threads.eqe.statistics", "true");
    }

    @Override
    public ExecutorWrapper doInitExecutorWrapper(WebServer webServer) {
        UndertowServletWebServer undertowServletWebServer = (UndertowServletWebServer) webServer;
        val undertow = (Undertow) ReflectionUtil.getFieldValue(UndertowServletWebServer.class,
                "undertow", undertowServletWebServer);
        if (Objects.isNull(undertow)) {
            return null;
        }
        XnioWorker xnioWorker = undertow.getWorker();

        Object taskPool = ReflectionUtil.getFieldValue(XnioWorker.class, "taskPool", xnioWorker);
        if (Objects.isNull(taskPool)) {
            return null;
        }
        val handler = TaskPoolHandlerFactory.getTaskPoolHandler(taskPool.getClass().getSimpleName());
        Object executor = ReflectionUtil.getFieldValue(taskPool.getClass(),
                handler.taskPoolType().getInternalExecutor(), taskPool);
        ExecutorWrapper executorWrapper = new ExecutorWrapper(POOL_NAME, handler.adapt(executor));
        if (executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutorProxy threadPoolExecutorProxy = new ThreadPoolExecutorProxy(executorWrapper);
            try {
                ReflectionUtil.setFieldValue(taskPool.getClass(),
                        handler.taskPoolType().getInternalExecutor(), taskPool, threadPoolExecutorProxy);
            } catch (IllegalAccessException e) {
                log.error("Undertow executor proxy exception", e);
            }
        }
        return executorWrapper;
    }

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(POOL_NAME, executors.get(getTpName()), dtpProperties.getPlatforms(), dtpProperties.getUndertowTp());
    }

    @Override
    protected String getTpName() {
        return POOL_NAME;
    }
}
