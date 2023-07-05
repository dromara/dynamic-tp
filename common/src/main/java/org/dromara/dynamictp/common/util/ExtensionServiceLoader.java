package org.dromara.dynamictp.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified ServiceLoader Help
 *
 * @author xs.Tao
 * @since 1.1.4
 */

public  class ExtensionServiceLoader {

    private static volatile Map<Class<?>, Object> extensionMap = new ConcurrentHashMap<>();

    private static volatile Map<Class<?>, List<?>> extensionListMap = new ConcurrentHashMap<>();

    private ExtensionServiceLoader() {
    }

    /**
     * loader service
     * @param clazz SPI interface
     * @return
     * @param <T>
     */
    public static <T> List<T> loader(Class<T> clazz) {
        List<T> services = (List<T>) extensionListMap.get(clazz);
        if (services == null) {
            services = reLoaderList(clazz);
            if (!services.isEmpty()) {
                extensionListMap.put(clazz, services);
            }
        }
        return services;
    }

    /**
     * loader the first service
     * @param clazz SPI interface
     * @return
     * @param <T>
     */
    public static <T>  T loaderFirst(Class<T> clazz){
        List<T> services=loader(clazz);
        return CollectionUtils.isEmpty(services)?null:services.get(0);
    }

    private static <T> List<T> reLoaderList(Class<T> clazz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
        List<T>          services    = new ArrayList<>();
        for (T service : serviceLoader) {
            services.add(service);
        }
        return services;
    }
}
