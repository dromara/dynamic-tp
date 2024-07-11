package org.dromara.dynamictp.common.manager;
import java.util.Map;
import java.util.ServiceLoader;

public class ContextManagerHelper {

    private static final ContextManager CONTEXT_MANAGER;

    static {
        ContextManager context = null;
        ServiceLoader<ContextManager> loader = ServiceLoader.load(ContextManager.class);
        for (ContextManager contextManager : loader) {
            context = contextManager;
            break;
        }
        if (context == null) {
            throw new IllegalStateException("No ContextManager implementation found");
        }
        CONTEXT_MANAGER = context;
    }

    public static <T> T getBean(Class<T> clazz) {
        return CONTEXT_MANAGER.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return CONTEXT_MANAGER.getBean(name, clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return CONTEXT_MANAGER.getBeansOfType(clazz);
    }

    public static void setContext(Object context) {
        CONTEXT_MANAGER.setContext(context);
    }

    public static void onEvent(Object event) {
        CONTEXT_MANAGER.onEvent(event);
    }

    public static Object getEnvironment() {
        return CONTEXT_MANAGER.getEnvironment();
    }

    public static String getEnvironmentProperty(String key) {
        return CONTEXT_MANAGER.getEnvironmentProperty(key);
    }

    public static String getEnvironmentProperty(String key, String defaultValue) {
        return CONTEXT_MANAGER.getEnvironmentProperty(key, defaultValue);
    }

}