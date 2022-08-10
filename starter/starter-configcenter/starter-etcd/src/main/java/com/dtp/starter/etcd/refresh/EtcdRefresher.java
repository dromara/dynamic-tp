package com.dtp.starter.etcd.refresh;

import com.dtp.common.config.DtpProperties;
import com.dtp.core.refresh.AbstractRefresher;
import com.dtp.core.support.PropertiesBinder;
import com.dtp.starter.etcd.util.EtcdUtil;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        Client client = EtcdUtil.client(etcd);
        String key = etcd.getKey();
        if (StringUtils.isNotBlank(key)) {
            loadConfig(etcd);
            initWatcher(client, key, etcd);
        } else {
            log.debug("rate limiter key is null, etcd client closed !");
            client.close();
        }
    }

    public void refresh(final DtpProperties dtpProperties) {
        doRefresh(dtpProperties);
    }

    /**
     * load config.
     * @param etcd {@link com.dtp.common.config.DtpProperties.Etcd}
     */
    private void loadConfig(final DtpProperties.Etcd etcd)
            throws IOException, ExecutionException, InterruptedException {
        Map<Object, Object> properties = EtcdUtil.getConfigContent(etcd, dtpProperties.getConfigType());
        PropertiesBinder.bindDtpProperties(properties, dtpProperties);
    }

    /**
     * init config watcher.
     * @param client {@link Client}
     * @param key config key
     * @param etcd {@link com.dtp.common.config.DtpProperties.Etcd}
     */
    private void initWatcher(final Client client, final String key, final DtpProperties.Etcd etcd) {
        client.getWatchClient().watch(ByteSequence.from(key, StandardCharsets.UTF_8),
                new EtcdListener(dtpProperties, key, this));
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
