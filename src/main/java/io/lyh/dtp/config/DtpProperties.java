package io.lyh.dtp.config;

import io.lyh.dtp.domain.NotifyPlatform;
import io.lyh.dtp.domain.ThreadPoolProperties;
import io.lyh.dtp.common.constant.DynamicTpConst;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * DtpProperties related
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
     * 开启动态线程池
     */
    private boolean enabled = true;

    /**
     * 是否打印banner
     */
    private boolean enabledBanner = true;

    /**
     * Nacos config
     */
    private Nacos nacos;

    /**
     * Apollo config
     */
    private Apollo apollo;

    /**
     * 配置文件类型
     */
    private String configType = "yml";

    /**
     * 开启指标采集
     */
    private boolean enabledCollect = false;

    /**
     * 监控时间间隔（s）
     */
    private int monitorInterval = 5;

    /**
     * 线程池配置
     */
    private List<ThreadPoolProperties> executors;

    /**
     * 通知平台配置
     */
    private List<NotifyPlatform> platforms;

    @Data
    public static class Nacos {

        /**
         * 监听的Nacos dataId
         */
        private String dataId;

        private String group = "DEFAULT_GROUP";;

        private String namespace = "public";
    }

    @Data
    public static class Apollo {

        private String namespace = "application";
    }
}
