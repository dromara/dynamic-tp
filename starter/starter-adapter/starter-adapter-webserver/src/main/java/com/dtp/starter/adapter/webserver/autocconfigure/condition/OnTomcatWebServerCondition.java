package com.dtp.starter.adapter.webserver.autocconfigure.condition;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

/**
 * @link Condition} that checks if the application is a Tomcat WebServer
 * @author liu.guorong
 * @Date 2022/8/7
 */
public class OnTomcatWebServerCondition extends AnyNestedCondition {

    public OnTomcatWebServerCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }
    @ConditionalOnBean(name = {"tomcatServletWebServerFactory"})
    static class ServletWebServer{}
    @ConditionalOnBean(name = {"tomcatReactiveWebServerFactory"})
    static class ReactiveWebServer{}
}
