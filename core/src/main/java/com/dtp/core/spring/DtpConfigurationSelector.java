package com.dtp.core.spring;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author <a href = "kamtohung@gmail.com">KamTo Hung</a>
 */

public class DtpConfigurationSelector implements DeferredImportSelector, Ordered {

    @Override
    @SuppressWarnings("all")
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{DtpBaseBeanConfiguration.class.getName(),
                DtpBeanDefinitionRegistrar.class.getName()};
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
