package com.dtp.starter.etcd.refresh;

import com.dtp.common.config.DtpProperties;
import com.dtp.core.refresh.AbstractRefresher;
import com.dtp.core.support.PropertiesBinder;
import com.dtp.starter.etcd.util.EtcdUtil;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;

/**
 * @author Redick01
 */
@Slf4j
public class EtcdRefresher extends AbstractRefresher implements InitializingBean, Ordered {

    @Resource
    private DtpProperties dtpProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        DtpProperties.Etcd etcd = dtpProperties.getEtcd();
        Map<Object, Object> map = loadConfig(etcd);
        if (map.size() > 0) {
            EtcdUtil.initWatcher(this, dtpProperties, map);
        }
    }

    public void refresh(final DtpProperties dtpProperties) {
        doRefresh(dtpProperties);
    }

    /**
     * load config.
     * @param etcd {@link com.dtp.common.config.DtpProperties.Etcd}
     */
    private Map<Object, Object> loadConfig(final DtpProperties.Etcd etcd) {
        Map<Object, Object> properties = EtcdUtil.getConfigMap(etcd, dtpProperties.getConfigType());
        PropertiesBinder.bindDtpProperties(properties, dtpProperties);
        return properties;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
