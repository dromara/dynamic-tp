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

package org.dromara.dynamictp.test.core.plugin;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.plugin.DtpInterceptor;
import org.dromara.dynamictp.common.plugin.DtpIntercepts;
import org.dromara.dynamictp.common.plugin.DtpInvocation;
import org.dromara.dynamictp.common.plugin.DtpSignature;

/**
 * @author hanli
 * @date 2023年07月19日 9:19 AM
 */
@DtpIntercepts(
        name = "TestAInterceptor",
        signatures = {
                @DtpSignature(clazz = InterceptTest.TestA.class, method = "beforeExecute", args = {}),
                @DtpSignature(clazz = InterceptTest.TestA.class, method = "afterExecute", args = {}),
                @DtpSignature(clazz = InterceptTest.TestA.class, method = "execute", args = {})
        }
)
@Slf4j
public class AInterceptorTest implements DtpInterceptor {

    private static final String BEFORE_EXECUTE = "beforeExecute";

    private static final String AFTER_EXECUTE = "afterExecute";

    @Override
    public Object intercept(DtpInvocation invocation) throws Throwable {
        String method = invocation.getMethod().getName();
        switch (method) {
            case BEFORE_EXECUTE:
                log.info("beforeExecute代理");
                break;
            case AFTER_EXECUTE:
                log.info("afterExecute代理");
                break;
            case "execute":
                log.info("execute代理");
                break;
            default:
                break;
        }
        return invocation.proceed();
    }

}
