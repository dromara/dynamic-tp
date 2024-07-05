package org.dromara.dynamictp.core.spring;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Key;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * GuiceContextHolder
 *
 * Provides similar functionality to Spring's ApplicationContextHolder using Google Guice.
 */
public class GuiceContextHolder {

    private static Injector injector;

    private GuiceContextHolder() {
        // Private constructor to prevent instantiation
    }

    public static void setInjector(Module... modules) {
        if (injector == null) {
            synchronized (GuiceContextHolder.class) {
                if (injector == null) {
                    injector = Guice.createInjector(modules);
                }
            }
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        return getInstance().getInstance(clazz);
    }

    public static <T> T getBean(Key<T> key) {
        return getInstance().getInstance(key);
    }

    public static <T> Map<Key<?>, T> getBeansOfType(Class<T> clazz) {
        return getInstance().getAllBindings().entrySet().stream()
                .filter(entry -> clazz.isAssignableFrom(entry.getKey().getTypeLiteral().getRawType()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (T) entry.getValue().getProvider().get()));
    }

    private static Injector getInstance() {
        if (injector == null) {
            throw new IllegalStateException("Injector is not initialized. Please set the injector before using it.");
        }
        return injector;
    }
}

