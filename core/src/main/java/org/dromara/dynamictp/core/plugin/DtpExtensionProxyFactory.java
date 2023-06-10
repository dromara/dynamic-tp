package org.dromara.dynamictp.core.plugin;

import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author windsearcher.lq
 * @since 2023/6/10 08:57
 */
public class DtpExtensionProxyFactory {

    public static Object wrap(Object target, DtpExtension interceptor) {
        // 代理target
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        // 不在注解所指定的类中，直接返回原对象
        if (!signatureMap.containsKey(target.getClass())) {
            return target;
        } else {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(target.getClass());
            enhancer.setCallback(new DtpExtensionProxy(target, interceptor, signatureMap));
            // 需要在DtpExecutor中添加一个默认无参构造函数
            return enhancer.create();
        }
    }

    private static Map<Class<?>, Set<Method>> getSignatureMap(DtpExtension interceptor) {
        DtpExtensionPoint interceptsAnnotation = interceptor.getClass().getAnnotation(DtpExtensionPoint.class);
        if (interceptsAnnotation == null) {
            throw new RuntimeException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
        }

        DtpSignature[] signatures = interceptsAnnotation.value();
        if (signatures == null) {
            throw new RuntimeException("@Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
        }

        Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
        for (DtpSignature signature : signatures) {
            Set<Method> methods = signatureMap.computeIfAbsent(signature.clazz(), k-> new HashSet<>());
            try {
                Method method = signature.clazz().getMethod(signature.method(), signature.args());
                methods.add(method);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Could not find method on " + signature.clazz() + " named " + signature.method() + ". Cause: " + e, e);
            }
        }
        return signatureMap;
    }
}
