package com.dtp.core.spring;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Objects;
import java.util.Properties;

/**
 * YamlPropertySourceFactory related
 *
 * @author yanhom
 * @since 1.1.0
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(encodedResource.getResource());
        Properties properties = factory.getObject();
        if (Objects.isNull(properties)
                || StringUtils.isBlank(encodedResource.getResource().getFilename())) {
            return null;
        }
        return new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
    }
}
