package com.dtp.core.spring;

import com.dtp.common.ApplicationContextHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * ApplicationContextHolderRegistrar related
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class ApplicationContextHolderRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String BEAN_NAME = "applicationContextHolder";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(BEAN_NAME)) {
            AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(ApplicationContextHolder.class,
                            ApplicationContextHolder::new)
                    .getBeanDefinition();
            registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
        }
    }

}
