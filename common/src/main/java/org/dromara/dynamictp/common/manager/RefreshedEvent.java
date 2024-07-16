package org.dromara.dynamictp.common.manager;

import org.dromara.dynamictp.common.properties.DtpProperties;

public class RefreshedEvent {
    private final Object source;

    public RefreshedEvent(Object source) {
        this.source = source;
    }

    public Object getSource() {
        return source;
    }

    // 提供默认实现，子类可以重写这个方法
    public DtpProperties getDtpProperties() {
        return null;
    }
}

