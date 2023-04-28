package org.dromara.dynamictp.common.event;

import org.dromara.dynamictp.common.properties.DtpProperties;
import org.springframework.context.ApplicationEvent;

/**
 * CollectEvent related
 *
 * @author yanhom
 * @since 1.0.0
 */
public class CollectEvent extends ApplicationEvent {

    private final transient DtpProperties dtpProperties;

    public CollectEvent(Object source, DtpProperties dtpProperties) {
        super(source);
        this.dtpProperties = dtpProperties;
    }

    public DtpProperties getDtpProperties() {
        return dtpProperties;
    }
}
