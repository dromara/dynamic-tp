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
import org.dromara.dynamictp.core.aware.AwareManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * RejectedInvocationHandler related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class RejectedInvocationHandler implements InvocationHandler {

    private final Object target;

    public RejectedInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        beforeReject((Runnable) args[0], (Executor) args[1]);
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        } finally {
            afterReject((Runnable) args[0], (Executor) args[1]);
        }
    }

    /**
     * Do sth before reject.
     *
     * @param runnable the runnable
     * @param executor ThreadPoolExecutor instance
     */
    private void beforeReject(Runnable runnable, Executor executor) {
        AwareManager.beforeReject(runnable, executor);
    }

    /**
     * Do sth after reject.
     *
     * @param runnable the runnable
     * @param executor ThreadPoolExecutor instance
     */
    private void afterReject(Runnable runnable, Executor executor) {
        AwareManager.afterReject(runnable, executor);
    }
}
