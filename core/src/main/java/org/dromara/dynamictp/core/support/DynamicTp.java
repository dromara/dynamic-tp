package org.dromara.dynamictp.core.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DynamicTp annotation, mainly used to manage juc ThreadPoolExecutor by this framework.
 *
 * @author yanhom
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
