package org.dromara.dynamictp.core.plugin;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * 代理包装类，代理需要进行扩展的类
 * @author windsearcher.lq
 * @since 2023/6/9 21:02
 */
public class DtpExtensionProxy implements MethodInterceptor {

    private Object target;

    private DtpExtension interceptor;

    private Map<Class<?>, Set<Method>> signatureMap;

    public DtpExtensionProxy(Object target, DtpExtension interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Set<Method> methods = signatureMap.get(method.getDeclaringClass());
        if (methods != null && methods.contains(method)) {
            return interceptor.intercept(new DtpInvocation(target, method, args));
        }

        return method.invoke(target, args);
    }
}
