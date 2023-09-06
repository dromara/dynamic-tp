package org.dromara.dynamictp.test.core.thread;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.plugin.DtpInterceptor;
import org.dromara.dynamictp.core.plugin.DtpIntercepts;
import org.dromara.dynamictp.core.plugin.DtpInvocation;
import org.dromara.dynamictp.core.plugin.DtpSignature;

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
public class TestAInterceptor implements DtpInterceptor {

    private static final String BEFORE_EXECUTE = "beforeExecute";

    private static final String AFTER_EXECUTE = "afterExecute";

    @Override
    public Object intercept(DtpInvocation invocation) throws Throwable {
        String method = invocation.getMethod().getName();
        Object[] args = invocation.getArgs();
        if (BEFORE_EXECUTE.equals(method)) {
            System.out.println("beforeExecute代理");
        } else if (AFTER_EXECUTE.equals(method)) {
            System.out.println("afterExecute代理");
        } else if ("execute".equals(method)) {
            System.out.println("execute代理");
        }

        return invocation.proceed();
    }


}
