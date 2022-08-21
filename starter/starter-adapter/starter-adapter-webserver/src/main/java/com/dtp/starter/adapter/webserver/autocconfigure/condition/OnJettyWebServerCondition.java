package com.dtp.starter.adapter.webserver.autocconfigure.condition;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Condition;

/**
 * {@link Condition} that checks if the application is a Jetty WebServer
 * @author liu.guorong
 * @since 1.0.8
 */
public class OnJettyWebServerCondition extends AnyNestedCondition {

    public OnJettyWebServerCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnBean(name = {"JettyServletWebServerFactory"})
    static class ServletWebServer{}

    @ConditionalOnBean(name = {"JettyReactiveWebServerFactory"})
    static class ReactiveWebServer{}
}
