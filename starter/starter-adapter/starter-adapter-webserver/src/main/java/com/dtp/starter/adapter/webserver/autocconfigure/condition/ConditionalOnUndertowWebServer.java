package com.dtp.starter.adapter.webserver.autocconfigure.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @link Condition} that checks if the application is a Undertow WebServer
 * @author liu.guorong
 * @Date 2022/8/7
 * @Description
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnUndertowWebServerCondition.class)
public @interface ConditionalOnUndertowWebServer {
}
