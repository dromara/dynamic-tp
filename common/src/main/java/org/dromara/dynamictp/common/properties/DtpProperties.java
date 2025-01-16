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

package org.dromara.dynamictp.common.properties;

import com.google.common.collect.Lists;
import lombok.Data;
import org.dromara.dynamictp.common.em.CollectorTypeEnum;
import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.entity.TpExecutorProps;

import java.util.List;

/**
 * Main properties that maintain by config center.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
public class DtpProperties {

    private DtpProperties() { }

    /**
     * If enabled DynamicTp.
     */
    private boolean enabled = true;

    /**
     * Environment, if not set, will use "APP.ENV".
     */
    private String env;

    /**
     * If print banner.
     */
    private boolean enabledBanner = true;

    /**
     * If enabled metrics collect.
     */
    private boolean enabledCollect = true;

    /**
     * Metrics collector types, default is logging.
     */
    private List<String> collectorTypes = Lists.newArrayList(CollectorTypeEnum.MICROMETER.name());

    /**
     * Metrics log storage path, just for "logging" type.
     */
    private String logPath;

    /**
     * Config file type, for zookeeper and etcd.
     */
    private String configType;

    /**
     * Monitor interval, time unit（s）
     */
    private int monitorInterval = 5;

    /**
     * Notify platform configs.
     */
    private List<NotifyPlatform> platforms;

    /**
     * Zookeeper config.
     */
    private Zookeeper zookeeper;

    /**
     * Etcd config.
     */
    private Etcd etcd;

    /**
     * ThreadPoolExecutor global configs.
     */
    private DtpExecutorProps globalExecutorProps;

    /**
     * ThreadPoolExecutor configs.
     */
    private List<DtpExecutorProps> executors;

    /**
     * Tomcat worker thread pool.
     */
    private TpExecutorProps tomcatTp;

    /**
     * Jetty thread pool.
     */
    private TpExecutorProps jettyTp;

    /**
     * Undertow thread pool.
     */
    private TpExecutorProps undertowTp;

    /**
     * Dubbo thread pools.
     */
    private List<TpExecutorProps> dubboTp;

    /**
     * Hystrix thread pools.
     */
    private List<TpExecutorProps> hystrixTp;

    /**
     * RocketMq thread pools.
     */
    private List<TpExecutorProps> rocketMqTp;

    /**
     * Grpc thread pools.
     */
    private List<TpExecutorProps> grpcTp;

    /**
     * Motan server thread pools.
     */
    private List<TpExecutorProps> motanTp;

    /**
     * Okhttp3 thread pools.
     */
    private List<TpExecutorProps> okhttp3Tp;

    /**
     * Brpc thread pools.
     */
    private List<TpExecutorProps> brpcTp;

    /**
     * Tars thread pools.
     */
    private List<TpExecutorProps> tarsTp;

    /**
     * Sofa thread pools.
     */
    private List<TpExecutorProps> sofaTp;

    /**
     * Rabbitmq thread pools.
     */
    private List<TpExecutorProps> rabbitmqTp;

    /**
     * Liteflow thread pools.
     */
    private List<TpExecutorProps> liteflowTp;
    
    public static DtpProperties getInstance() {
        return Holder.INSTANCE;
    }

    @Data
    public static class Zookeeper {

        private String zkConnectStr;

        private String configVersion;

        private String rootNode;

        private String node;

        private String configKey;
    }

    /**
     * Etcd config.
     */
    @Data
    public static class Etcd {

        private String endpoints;

        private String user;

        private String password;

        private String charset = "UTF-8";

        private boolean authEnable = false;

        private String authority = "ssl";

        private String key;

        private long timeout = 30000L;
    }
    
    private static class Holder {
        private static final DtpProperties INSTANCE = new DtpProperties();
    }
}
