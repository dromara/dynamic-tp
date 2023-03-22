package com.dtp.core.spring;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author <a href = "kamtohung@gmail.com">KamTo Hung</a>
 */

public class DtpConfigurationSelector implements DeferredImportSelector, Ordered {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{DtpBeanDefinitionRegistrar.class.getName(),
                BaseBeanAutoConfiguration.class.getName()};
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
