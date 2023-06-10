package org.dromara.dynamictp.core.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author windsearcher.lq
 * @since 2023/6/9 20:47
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DtpExtensionPoint {

    DtpSignature[] value();

}
