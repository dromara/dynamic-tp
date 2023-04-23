package org.dromara.dynamictp.starter.adapter.webserver.autocconfigure.condition;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Condition;

/**
 * {@link Condition} that checks if the application is an Undertow WebServer
 *
 * @author liu.guorong
 * @since 1.0.8
 */
public class OnUndertowWebServerCondition extends AnyNestedCondition {

    public OnUndertowWebServerCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnBean(name = {"undertowServletWebServerFactory"})
    static class ServletWebServer { }

    @ConditionalOnBean(name = {"undertowReactiveWebServerFactory"})
    static class ReactiveWebServer { }
}
