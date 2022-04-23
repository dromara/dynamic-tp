package com.dtp.core.support;

import com.dtp.common.config.DtpProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;

import java.util.Map;

import static com.dtp.common.constant.DynamicTpConst.MAIN_PROPERTIES_PREFIX;

/**
 * PropertiesBinder related
 *
 * @author: yanhom
 * @since 1.0.3
 **/
public class PropertiesBinder {

    private PropertiesBinder() {}

    public static void bindDtpProperties(Map<?, Object> properties, DtpProperties dtpProperties) {
        ConfigurationPropertySource sources = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(sources);
        ResolvableType type = ResolvableType.forClass(DtpProperties.class);
        Bindable<?> target = Bindable.of(type).withExistingValue(dtpProperties);
        binder.bind(MAIN_PROPERTIES_PREFIX, target);
    }

    public static void bindDtpProperties(Environment environment, DtpProperties dtpProperties) {
        Binder binder = Binder.get(environment);
        ResolvableType type = ResolvableType.forClass(DtpProperties.class);
        Bindable<?> target = Bindable.of(type).withExistingValue(dtpProperties);
        binder.bind(MAIN_PROPERTIES_PREFIX, target);
    }
}
