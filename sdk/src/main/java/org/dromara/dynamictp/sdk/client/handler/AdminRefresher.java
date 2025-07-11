package org.dromara.dynamictp.sdk.client.handler;


import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.refresher.AbstractRefresher;
import org.omg.CORBA.Environment;

import java.util.Map;

public class AdminRefresher extends AbstractRefresher {

    public AdminRefresher(DtpProperties dtpProperties) {
        super(dtpProperties);
    }

    @Override
    public void refresh(Map<Object, Object> properties) {
        super.refresh(properties);
    }

    public void refresh(Environment environment) {
        super.refresh(environment);
    }
}
