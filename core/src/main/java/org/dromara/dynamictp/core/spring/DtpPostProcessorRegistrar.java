package org.dromara.dynamictp.core.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * DtpPostProcessorRegistrar related
 *
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class DtpPostProcessorRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String BEAN_NAME = "dtpPostProcessor";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(BEAN_NAME)) {
            AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(DtpPostProcessor.class,
                            DtpPostProcessor::new)
                    .getBeanDefinition();
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            beanDefinition.setSynthetic(true);
            registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
        }
    }

}
