package com.dtp.starter.adapter.dubbo.autoconfigure.condition;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Condition;

/**
 * {@link Condition} that checks if the application is a dubbo application
 *
 * @author yanhom
 * @since 1.0.6
 */
public class OnApacheDubboCondition extends AnyNestedCondition {

    OnApacheDubboCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnBean(type = "org.apache.dubbo.config.spring.beans.factory.annotation.ServiceClassPostProcessor")
    static class ServiceClassBpp {}

    @ConditionalOnBean(type = "org.apache.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationPostProcessor")
    static class ServiceAnnotationBpp {}
}