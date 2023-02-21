package com.dtp.starter.etcd.refresh;

import com.dtp.common.properties.DtpProperties;
import com.dtp.core.spring.PropertiesBinder;
import com.dtp.starter.etcd.util.EtcdUtil;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author Redick01
 */
@Slf4j
public class EtcdListener implements Watch.Listener {

    private final DtpProperties dtpProperties;

    private final String key;

    private final EtcdRefresher etcdRefresher;

    public EtcdListener(DtpProperties dtpProperties, String key, EtcdRefresher etcdRefresher) {
        this.dtpProperties = dtpProperties;
        this.key = key;
        this.etcdRefresher = etcdRefresher;
    }

    @SneakyThrows
    @Override
    public void onNext(WatchResponse response) {
        log.info("etcd config content updated, key is " + key);
        WatchEvent.EventType eventType = response.getEvents().get(0).getEventType();
        if (WatchEvent.EventType.PUT.equals(eventType)) {
            log.info("the etcd config content should be updated, key is " + key);
            String configType = dtpProperties.getConfigType();
            val properties = EtcdUtil.watchValMap(configType, response.getEvents(), dtpProperties);
            PropertiesBinder.bindDtpProperties(properties, dtpProperties);
            etcdRefresher.refresh(dtpProperties);
        } else {
            log.info("the etcd config content should not be updated, key is " + key);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("etcd config watcher exception !", throwable);
    }

    @Override
    public void onCompleted() {
        log.info("etcd config key refreshed, config key is : " + key);
    }
}
