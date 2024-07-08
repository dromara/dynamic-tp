package org.dromara.dynamictp.core.spring;

import org.dromara.dynamictp.common.spring.ContextHolder;
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;

import java.util.Map;
import java.util.ServiceLoader;

public class ContextHolderHelper {

    private static final ContextHolder CONTEXT_HOLDER;

    static {
        ContextHolder holder = null;
        ServiceLoader<ContextHolder> loader = ServiceLoader.load(ContextHolder.class);
        for (ContextHolder contextHolder : loader) {
            if (contextHolder.getClass().getName().equals("org.dromara.dynamictp.spring.ex.SpringContextHolder")) {
                holder = contextHolder;
                break;
            } else {
                holder = contextHolder;
            }
        }
        if (holder == null) {
            throw new IllegalStateException("No ContextHolder implementation found");
        }
        CONTEXT_HOLDER = holder;
    }

    public static <T> T getBean(Class<T> clazz) {
        return CONTEXT_HOLDER.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return CONTEXT_HOLDER.getBean(name, clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return CONTEXT_HOLDER.getBeansOfType(clazz);
    }

    public static void publishEvent(Object event) {
        CONTEXT_HOLDER.publishEvent(event);
    }
}