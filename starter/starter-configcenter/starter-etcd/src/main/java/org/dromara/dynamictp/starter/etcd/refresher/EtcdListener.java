/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.starter.etcd.refresher;

import io.etcd.jetcd.Watch;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.support.binder.BinderHelper;
import org.dromara.dynamictp.starter.etcd.util.EtcdUtil;

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
            BinderHelper.bindDtpProperties(properties, dtpProperties);
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
