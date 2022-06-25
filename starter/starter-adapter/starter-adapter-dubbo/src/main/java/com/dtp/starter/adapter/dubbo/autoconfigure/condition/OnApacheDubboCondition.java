package com.dtp.starter.adapter.dubbo.autoconfigure.condition;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    @ConditionalOnProperty(name = "dubbo.enabled", matchIfMissing = true)
    static class PropertyEnabled {}

    @ConditionalOnProperty(name = "dubbo.application.name")
    static class DubboAppName {}

    @ConditionalOnProperty(name = "dubbo.registry.address")
    static class DubboRegistryAddress {}
}