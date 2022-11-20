package com.dtp.starter.adapter.common.autoconfigure;

import com.dtp.adapter.common.DtpAdapterListener;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.constant.DynamicTpConst;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * AdapterCommonAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.9
 **/
@Configuration
@EnableConfigurationProperties(DtpProperties.class)
@ConditionalOnProperty(name = DynamicTpConst.DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
public class AdapterCommonAutoConfiguration {

    @Bean
    @DependsOn({"dtpApplicationContextHolder"})
    @ConditionalOnMissingBean
    public DtpAdapterListener dtpAdapterListener() {
        return new DtpAdapterListener();
    }
}
