package org.dromara.dynamictp.common.properties;

import org.dromara.dynamictp.common.constant.DynamicTpConst;
import org.dromara.dynamictp.common.entity.DtpExecutorProps;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.em.CollectorTypeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Main properties that maintain by config center.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
@Data
@ConfigurationProperties(prefix = DynamicTpConst.MAIN_PROPERTIES_PREFIX)
public class DtpProperties {

    /**
     * If enabled DynamicTp.
     */
    private boolean enabled = true;

    /**
     * If print banner.
     */
    private boolean enabledBanner = true;

    /**
     * Config file type.
     */
    private String configType = "yml";

    /**
     * If enabled metrics collect.
     */
    private boolean enabledCollect = false;

    /**
     * Metrics collector types, default is logging.
     */
    private List<String> collectorTypes = Lists.newArrayList(CollectorTypeEnum.MICROMETER.name());

    /**
     * Metrics log storage path, just for "logging" type.
     */
    private String logPath;

    /**
     * Monitor interval, time unit（s）
     */
    private int monitorInterval = 5;

    /**
     * Notify platform configs.
     */
    private List<NotifyPlatform> platforms;

    /**
     * Nacos config.
     */
    private Nacos nacos;

    /**
     * Apollo config.
     */
    private Apollo apollo;

    /**
     * Zookeeper config.
     */
    private Zookeeper zookeeper;

    /**
     * Etcd config.
     */
    private Etcd etcd;

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

    @Data
    public static class Nacos {

        private String dataId;

        private String group;

        private String namespace;
    }

    @Data
    public static class Apollo {

        private String namespace;
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
    }
}
