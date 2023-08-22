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

package org.dromara.dynamictp.starter.adapter.webserver.adapter.proxy;

import io.micrometer.core.instrument.binder.jetty.InstrumentedQueuedThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.plugin.DtpInterceptor;
import org.dromara.dynamictp.core.plugin.DtpIntercepts;
import org.dromara.dynamictp.core.plugin.DtpInvocation;
import org.dromara.dynamictp.core.plugin.DtpSignature;
import org.dromara.dynamictp.core.support.EnhanceRunnable;
import org.eclipse.jetty.util.thread.MonitoredQueuedThreadPool;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * JettyDtpInterceptor related
 *
 * @author kyao
 */
@DtpIntercepts(
        name = "jettyDtpInterceptor",
        signatures = {
            @DtpSignature(clazz = QueuedThreadPool.class, method = "execute", args = {Runnable.class}),
            @DtpSignature(clazz = InstrumentedQueuedThreadPool.class, method = "execute", args = {Runnable.class}),
            @DtpSignature(clazz = MonitoredQueuedThreadPool.class, method = "execute", args = {Runnable.class})
        }
)
@Slf4j
public class JettyDtpInterceptor implements DtpInterceptor {

    @Override
    public Object intercept(DtpInvocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        Runnable runnable = (Runnable) args[0];
        Executor executor = (Executor) invocation.getTarget();
        EnhanceRunnable enhanceRunnable = EnhanceRunnable.of(runnable, executor);
        args[0] = enhanceRunnable;
        try {
            return invocation.proceed();
        } catch (RejectedExecutionException e) {
            AwareManager.beforeReject(runnable, executor, log);
            throw e;
        }
    }

}
