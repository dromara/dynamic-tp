package com.dtp.starter.adapter.dubbo.autoconfigure.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * {@link Conditional @Conditional} that only matches when the application is a dubbo application.
 *
 * @author yanhom
 * @since 1.0.6
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnApacheDubboCondition.class)
public @interface ConditionOnApacheDubboApp {

}