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

package org.dromara.dynamictp.starter.zookeeper.refresher;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.WatchedEvent;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.spring.AbstractSpringRefresher;
import org.dromara.dynamictp.starter.zookeeper.autoconfigure.ZkConfigEnvironmentProcessor;
import org.dromara.dynamictp.starter.zookeeper.util.CuratorUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * @author Redick01
 */
@Slf4j
public class ZookeeperRefresher extends AbstractSpringRefresher implements EnvironmentAware, InitializingBean {

    public ZookeeperRefresher(DtpProperties dtpProperties) {
        super(dtpProperties);
    }

    @Override
    public void afterPropertiesSet() {

        final ConnectionStateListener connectionStateListener = (client, newState) -> {
            if (newState == ConnectionState.RECONNECTED) {
                loadAndRefresh();
            }
        };

        final CuratorListener curatorListener = (client, curatorEvent) -> {
            final WatchedEvent watchedEvent = curatorEvent.getWatchedEvent();
            if (null != watchedEvent) {
                switch (watchedEvent.getType()) {
                    case NodeChildrenChanged:
                    case NodeDataChanged:
                        loadAndRefresh();
                        break;
                    default:
                        break;
                }
            }
        };

        CuratorFramework curatorFramework = CuratorUtil.getCuratorFramework(dtpProperties);
        String nodePath = CuratorUtil.nodePath(dtpProperties);

        curatorFramework.getConnectionStateListenable().addListener(connectionStateListener);
        curatorFramework.getCuratorListenable().addListener(curatorListener);
        cleanZkPropertySource(environment);
        log.info("DynamicTp refresher, add listener success, nodePath: {}", nodePath);
    }

    /**
     * load config and refresh
     */
    private void loadAndRefresh() {
        refresh(CuratorUtil.genPropertiesMap(dtpProperties));
    }

    /**
     * ZK_PROPERTY_SOURCE just for DtpBeanDefinitionRegistrar
     *
     * @param environment environment
     */
    private void cleanZkPropertySource(Environment environment) {
        ConfigurableEnvironment env = ((ConfigurableEnvironment) environment);
        env.getPropertySources().remove(ZkConfigEnvironmentProcessor.ZK_PROPERTY_SOURCE_NAME);
    }
}
