package com.dtp.starter.adapter.webserver.autocconfigure.condition;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

/**
 * @link Condition} that checks if the application is a Undertow WebServer
 * @author liu.guorong
 * @Date 2022/8/7
 * @Description
 */
public class OnUndertowWebServerCondition extends AnyNestedCondition {

    public OnUndertowWebServerCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }
    @ConditionalOnBean(name = {"undertowServletWebServerFactory"})
    static class ServletWebServer{}
    @ConditionalOnBean(name = {"undertowReactiveWebServerFactory"})
    static class ReactiveWebServer{}
}
