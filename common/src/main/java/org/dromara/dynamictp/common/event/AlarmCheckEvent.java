package org.dromara.dynamictp.common.event;

import org.dromara.dynamictp.common.properties.DtpProperties;
import org.springframework.context.ApplicationEvent;

/**
 * AlarmCheckEvent related
 *
 * @author yanhom
 * @since 1.0.0
 */
public class AlarmCheckEvent extends ApplicationEvent {

    private final transient DtpProperties dtpProperties;

    public AlarmCheckEvent(Object source, DtpProperties dtpProperties) {
        super(source);
        this.dtpProperties = dtpProperties;
    }

    public DtpProperties getDtpProperties() {
        return dtpProperties;
    }
}
