package org.dromara.dynamictp.core.plugin;

/**
 * 签名，用于指定具体需要拦截的类和对应的方法
 * @author windsearcher.lq
 * @since 2023/6/9 20:53
 */
public @interface DtpSignature {

    Class<?> clazz();

    String method();

    Class<?>[] args();

}
