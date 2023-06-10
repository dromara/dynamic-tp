package org.dromara.dynamictp.core.plugin;


import java.lang.reflect.InvocationTargetException;

public interface DtpExtension {

    Object intercept(DtpInvocation invocation) throws InvocationTargetException, IllegalAccessException;

    default Object plugin(Object target) {
        return DtpExtensionProxyFactory.wrap(target, this);
    }

}
