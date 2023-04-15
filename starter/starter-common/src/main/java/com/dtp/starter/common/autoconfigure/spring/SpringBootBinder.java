package com.dtp.starter.common.autoconfigure.spring;

import com.dtp.common.properties.DtpProperties;
import com.dtp.core.spring.PropertiesBinder;
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
 * @author yanhom
 * @since 1.0.3
 **/
public class SpringBootBinder implements PropertiesBinder {

    @Override
    public DtpProperties bindDtpProperties(Map<?, Object> properties, DtpProperties dtpProperties) {
        ConfigurationPropertySource sources = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(sources);
        ResolvableType type = ResolvableType.forClass(DtpProperties.class);
        Bindable<?> target = Bindable.of(type).withExistingValue(dtpProperties);
        binder.bind(MAIN_PROPERTIES_PREFIX, target);
        return dtpProperties;
    }
    
    @Override
    public DtpProperties bindDtpProperties(Environment environment, DtpProperties dtpProperties) {
        Binder binder = Binder.get(environment);
        ResolvableType type = ResolvableType.forClass(DtpProperties.class);
        Bindable<?> target = Bindable.of(type).withExistingValue(dtpProperties);
        binder.bind(MAIN_PROPERTIES_PREFIX, target);
        return dtpProperties;
    }
}
