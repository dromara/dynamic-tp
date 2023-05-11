package org.dromara.dynamictp.core.spring;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.util.Singleton;

import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * BinderHelper related
 *
 * @author dragon-zhang
 * @since 1.1.4
 */
@Slf4j
public class BinderHelper {
    
    public static PropertiesBinder getBinder() {
        PropertiesBinder binder = Singleton.INST.get(PropertiesBinder.class);
        if (Objects.nonNull(binder)) {
            return binder;
        }
        final Iterator<PropertiesBinder> iterator = ServiceLoader.load(PropertiesBinder.class).iterator();
        if (!iterator.hasNext()) {
            log.error("DynamicTp refresh, no SPI for org.dromara.dynamictp.core.spring.PropertiesBinder.");
            return null;
        }
        binder = iterator.next();
        Singleton.INST.single(PropertiesBinder.class, binder);
        return binder;
    }
}
