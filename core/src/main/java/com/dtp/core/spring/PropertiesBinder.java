package com.dtp.core.spring;

import com.dtp.common.properties.DtpProperties;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * PropertiesBinder related
 *
 * @author yanhom
 * @since 1.0.3
 **/
public interface PropertiesBinder {

    DtpProperties bindDtpProperties(Map<?, Object> properties, DtpProperties dtpProperties);

    DtpProperties bindDtpProperties(Environment environment, DtpProperties dtpProperties);
}
