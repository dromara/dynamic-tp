package org.dromara.dynamictp.core.spring;

import org.dromara.dynamictp.common.ApplicationContextHolder;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.DTP_ENABLED_PROP;

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
        return new String[]{
                DtpBeanDefinitionRegistrar.class.getName(),
                DtpPostProcessorRegistrar.class.getName(),
                ApplicationContextHolder.class.getName(),
                DtpBaseBeanConfiguration.class.getName()
        };
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
