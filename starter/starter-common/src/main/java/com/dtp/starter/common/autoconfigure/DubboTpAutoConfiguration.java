package com.dtp.starter.common.autoconfigure;

import com.dtp.adapter.rpc.dubbo.DtpDubboCollectListener;
import com.dtp.adapter.rpc.dubbo.DtpDubboRefreshListener;
import com.dtp.adapter.rpc.dubbo.DubboTpHandler;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.constant.DynamicTpConst;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.dtp.starter.common.autoconfigure.DubboTpAutoConfiguration.DUBBO_PREFIX;

/**
 * DubboTpAutoConfiguration related
 *
 * @author yanhom
 */
@Configuration
@EnableConfigurationProperties(DtpProperties.class)
@ConditionalOnProperty(name = {DynamicTpConst.DTP_ENABLED_PROP, DUBBO_PREFIX}, matchIfMissing = true)
public class DubboTpAutoConfiguration {

    public static final String DUBBO_PREFIX = "dubbo.enabled";

    @Bean
    public DubboTpHandler dubboTpHandler() {
        return new DubboTpHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public DtpDubboRefreshListener dtpDubboRefreshListener() {
        return new DtpDubboRefreshListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public DtpDubboCollectListener dtpDubboCollectListener() {
        return new DtpDubboCollectListener();
    }
}
