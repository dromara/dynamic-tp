package org.dromara.dynamictp.test.common.util;

import org.dromara.dynamictp.core.plugin.DtpInterceptor;
import org.dromara.dynamictp.core.plugin.DtpInvocation;

public  class  TestInterceptorLoader implements DtpInterceptor {

        @Override
        public Object intercept(DtpInvocation invocation) throws Throwable {
            return invocation.proceed();
        }
    }