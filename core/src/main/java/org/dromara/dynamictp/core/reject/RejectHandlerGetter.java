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

package org.dromara.dynamictp.core.reject;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.ex.DtpException;
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import static org.dromara.dynamictp.common.em.RejectedTypeEnum.ABORT_POLICY;
import static org.dromara.dynamictp.common.em.RejectedTypeEnum.CALLER_RUNS_POLICY;
import static org.dromara.dynamictp.common.em.RejectedTypeEnum.DISCARD_OLDEST_POLICY;
import static org.dromara.dynamictp.common.em.RejectedTypeEnum.DISCARD_POLICY;

/**
 * RejectHandlerGetter related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class RejectHandlerGetter {

    private RejectHandlerGetter() { }

    public static RejectedExecutionHandler buildRejectedHandler(String name) {
        if (Objects.equals(name, ABORT_POLICY.getName())) {
            return new ThreadPoolExecutor.AbortPolicy();
        } else if (Objects.equals(name, CALLER_RUNS_POLICY.getName())) {
            return new ThreadPoolExecutor.CallerRunsPolicy();
        } else if (Objects.equals(name, DISCARD_OLDEST_POLICY.getName())) {
            return new ThreadPoolExecutor.DiscardOldestPolicy();
        } else if (Objects.equals(name, DISCARD_POLICY.getName())) {
            return new ThreadPoolExecutor.DiscardPolicy();
        }
        List<RejectedExecutionHandler> loadedHandlers = ExtensionServiceLoader.get(RejectedExecutionHandler.class);
        for (RejectedExecutionHandler handler : loadedHandlers) {
            String handlerName = handler.getClass().getSimpleName();
            if (name.equalsIgnoreCase(handlerName)) {
                return handler;
            }
        }

        log.error("Cannot find specified rejectedHandler {}", name);
        throw new DtpException("Cannot find specified rejectedHandler " + name);
    }

    public static RejectedExecutionHandler getProxy(String name) {
        return getProxy(buildRejectedHandler(name));
    }

    public static RejectedExecutionHandler getProxy(RejectedExecutionHandler handler) {
        return (RejectedExecutionHandler) Proxy
                .newProxyInstance(handler.getClass().getClassLoader(),
                        new Class[]{RejectedExecutionHandler.class},
                        new RejectedInvocationHandler(handler));
    }
}
