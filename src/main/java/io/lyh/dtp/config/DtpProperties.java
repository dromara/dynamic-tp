package io.lyh.dtp.config;

import io.lyh.dtp.common.constant.DynamicTpConst;
import io.lyh.dtp.notify.NotifyPlatform;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Main properties that maintain by the config center.
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-27 17:37
 * @since 1.0.0
 **/
@Slf4j
@Data
@Configuration
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
     * Config file type.
     */
    private String configType = "yml";

    /**
     * If enabled metrics collect.
     */
    private boolean enabledCollect = false;

    /**
     * Monitor interval, time unit（s）
     */
    private int monitorInterval = 5;

    /**
     * ThreadPoolExecutor configs.
     */
    private List<ThreadPoolProperties> executors;

    /**
     * Notify platform configs.
     */
    private List<NotifyPlatform> platforms;

    @Data
    public static class Nacos {

        private String dataId;

        private String group = "DEFAULT_GROUP";;

        private String namespace = "public";
    }

    @Data
    public static class Apollo {

        private String namespace = "application";
    }
}
