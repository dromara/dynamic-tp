package com.dtp.starter.adapter.webserver.autocconfigure.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Conditional @Conditional} that checks if the application is a Tomcat WebServer
 *
 * @author liu.guorong
 * @Date 2022/8/7
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnTomcatWebServerCondition.class)
public @interface ConditionalOnTomcatWebServer {
}
