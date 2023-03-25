package com.dtp.core.spring;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;

/**
 * DtpConfigurationSelector related
 *
 * @author KamTo Hung
 * @since 1.1.1
 */
public class DtpConfigurationSelector implements DeferredImportSelector, Ordered {

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        return new String[]{DtpBaseBeanConfiguration.class.getName(),
                DtpBeanDefinitionRegistrar.class.getName()};
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
