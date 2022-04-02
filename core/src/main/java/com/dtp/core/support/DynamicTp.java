package com.dtp.core.support;

import java.lang.annotation.*;

/**
 * DynamicTp annotation, mainly used to manage juc ThreadPoolExecutor by this framework.
 *
 * @author: yanhom
 * @since 1.0.3
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicTp {

    /**
     * Thread pool name, has higher priority than @Bean annotated method name.
     *
     * @return name
     */
    String value() default "";
}
