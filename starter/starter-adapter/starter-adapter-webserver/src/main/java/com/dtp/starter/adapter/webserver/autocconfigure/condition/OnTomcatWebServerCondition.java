package com.dtp.starter.adapter.webserver.autocconfigure.condition;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Condition;

/**
 * {@link Condition} that checks if the application is a Tomcat WebServer
 * @author liu.guorong
 * @since 1.0.8
 */
public class OnTomcatWebServerCondition extends AnyNestedCondition {

    public OnTomcatWebServerCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnBean(name = {"tomcatServletWebServerFactory"})
    static class ServletWebServer { }

    @ConditionalOnBean(name = {"tomcatReactiveWebServerFactory"})
    static class ReactiveWebServer { }
}
