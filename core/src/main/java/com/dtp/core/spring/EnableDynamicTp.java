package com.dtp.core.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableDynamicTp related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(DtpBeanDefinitionRegistrar.class)
public @interface EnableDynamicTp {
}
