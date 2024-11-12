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

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.support.binder.BinderHelper;
import org.dromara.dynamictp.spring.AbstractSpringRefresher;
import org.dromara.dynamictp.starter.etcd.util.EtcdUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;

import java.util.Map;

/**
 * @author Redick01
 */
@Slf4j
public class EtcdRefresher extends AbstractSpringRefresher implements InitializingBean, Ordered, DisposableBean {

    public EtcdRefresher(DtpProperties dtpProperties) {
        super(dtpProperties);
    }

    @Override
    public void afterPropertiesSet() {
        DtpProperties.Etcd etcd = dtpProperties.getEtcd();
        Map<Object, Object> map = loadConfig(etcd);
        if (!map.isEmpty()) {
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
        BinderHelper.bindDtpProperties(properties, dtpProperties);
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
