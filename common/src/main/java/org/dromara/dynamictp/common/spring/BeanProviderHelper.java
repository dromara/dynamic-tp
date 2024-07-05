package org.dromara.dynamictp.common.spring;

import org.dromara.dynamictp.common.util.ExtensionServiceLoader;

import java.util.Map;

public class BeanProviderHelper {

    private static BeanProvider provider;

    static {
        provider = ExtensionServiceLoader.getFirst(BeanProvider.class);
        if (provider == null) {
            throw new IllegalStateException("No BeanProvider implementation found");
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        return provider.getBean(clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return provider.getBeansOfType(clazz);
    }
}
