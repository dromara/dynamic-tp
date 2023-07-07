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

package com.dromara.dynamictp.extension.ttl;

import com.alibaba.ttl.TtlRunnable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dromara.dynamictp.core.plugin.DtpInterceptor;
import org.dromara.dynamictp.core.plugin.DtpIntercepts;
import org.dromara.dynamictp.core.plugin.DtpInvocation;
import org.dromara.dynamictp.core.plugin.DtpSignature;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import org.dromara.dynamictp.core.thread.DtpExecutor;

import java.lang.reflect.InvocationTargetException;

/**
 * TtlDtpInterceptor related
 *
 * @author yanhom
 * @since 1.1.4
 **/
@DtpIntercepts({
        @DtpSignature(clazz = DtpExecutor.class, method = "beforeExecute", args = {Thread.class, Runnable.class}),
        @DtpSignature(clazz = DtpExecutor.class, method = "afterExecute", args = {Runnable.class, Throwable.class})
})
@Slf4j
public class TtlDtpInterceptor implements DtpInterceptor {

    private static final String BEFORE_EXECUTE = "beforeExecute";

    private static final String AFTER_EXECUTE = "afterExecute";

    @Override
    public Object intercept(DtpInvocation invocation) throws InvocationTargetException, IllegalAccessException {

        DtpExecutor dtpExecutor = (DtpExecutor) invocation.getTarget();
        String method = invocation.getMethod().getName();
        Object[] args = invocation.getArgs();
        if ((BEFORE_EXECUTE.equals(method) && isDtpRunnable(args[1])) ||
                (AFTER_EXECUTE.equals(method) && isDtpRunnable(args[0]))) {
            return invocation.proceed();
        }
        if (BEFORE_EXECUTE.equals(method)) {
            processBeforeExecute(dtpExecutor, args);
        } else if (AFTER_EXECUTE.equals(method)) {
            processAfterExecute(args);
        }
        return invocation.proceed();
    }

    private boolean isDtpRunnable(Object runnable) {
        return runnable instanceof DtpRunnable;
    }

    private void processBeforeExecute(DtpExecutor dtpExecutor, Object[] args) {
        if (!(args[1] instanceof TtlRunnable)) {
            return;
        }
        TtlRunnable ttlRunnable = (TtlRunnable) args[1];
        val innerRunnable = ttlRunnable.getRunnable();
        if (innerRunnable instanceof DtpRunnable) {
            DtpRunnable runnable = (DtpRunnable) innerRunnable;
            runnable.cancelQueueTimeoutTask();
            runnable.startRunTimeoutTask(dtpExecutor, (Thread) args[0]);
        }
    }

    private void processAfterExecute(Object[] args) {
        if (!(args[0] instanceof TtlRunnable)) {
            return;
        }
        TtlRunnable ttlRunnable = (TtlRunnable) args[0];
        val innerRunnable = ttlRunnable.getRunnable();
        if (innerRunnable instanceof DtpRunnable) {
            DtpRunnable runnable = (DtpRunnable) innerRunnable;
            runnable.cancelRunTimeoutTask();
        }
    }
}
