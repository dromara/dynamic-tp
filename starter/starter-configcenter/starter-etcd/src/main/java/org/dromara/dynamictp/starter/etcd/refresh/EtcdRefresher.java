package org.dromara.dynamictp.starter.etcd.refresh;

import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.refresher.AbstractRefresher;
import org.dromara.dynamictp.core.spring.PropertiesBinder;
import org.dromara.dynamictp.starter.etcd.util.EtcdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;

import java.util.Map;

/**
 * @author Redick01
 */
@Slf4j
public class EtcdRefresher extends AbstractRefresher implements InitializingBean, Ordered, DisposableBean {

    public EtcdRefresher(DtpProperties dtpProperties) {
        super(dtpProperties);
    }

    @Override
    public void afterPropertiesSet() {
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
     * @param etcd {@link DtpProperties.Etcd}
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

    @Override
    public void destroy() {
        EtcdUtil.close();
    }

}
