package com.dtp.common.config;

import com.dtp.common.config.web.JettyThreadPool;
import com.dtp.common.config.web.TomcatThreadPool;
import com.dtp.common.config.web.UndertowThreadPool;
import com.dtp.common.constant.DynamicTpConst;
import com.dtp.common.dto.NotifyPlatform;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.List;

/**
 * Main properties that maintain by config center.
 *
 * @author: yanhom
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
     * Config file type.
     */
    private String configType = "yml";

    /**
     * If enabled metrics collect.
     */
    private boolean enabledCollect = false;

    /**
     * Metrics collector type.
     */
    public String collectorType = "logging";

    /**
     * MetricsLog storage path
     */
    public String logPath;

    /**
     * Monitor interval, time unit（s）
     */
    private int monitorInterval = 5;

    /**
     * ThreadPoolExecutor configs.
     */
    private List<ThreadPoolProperties> executors;

    /**
     * Tomcat worker thread pool.
     */
    private TomcatThreadPool tomcatTp;

    /**
     * Jetty thread pool.
     */
    private JettyThreadPool jettyTp;

    /**
     * Undertow thread pool.
     */
    private UndertowThreadPool undertowTp;

    /**
     * Notify platform configs.
     */
    private List<NotifyPlatform> platforms;

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
}
