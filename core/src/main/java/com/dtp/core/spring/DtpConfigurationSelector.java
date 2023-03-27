package com.dtp.core.spring;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import static com.dtp.common.constant.DynamicTpConst.DTP_ENABLED_PROP;

/**
 * DtpConfigurationSelector related
 *
 * @author KamTo Hung
 * @since 1.1.1
 */
public class DtpConfigurationSelector implements DeferredImportSelector, Ordered, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        if (!BooleanUtils.toBoolean(environment.getProperty(DTP_ENABLED_PROP, BooleanUtils.TRUE))) {
            return new String[]{};
        }
        return new String[]{DtpBaseBeanConfiguration.class.getName(),
                DtpBeanDefinitionRegistrar.class.getName()};
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
