package com.dtp.starter.adapter.tars.autoconfigure;

import com.dtp.adapter.tars.TarsDtpAdapter;
import com.dtp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TarsTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Configuration
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@ConditionalOnClass(name = "com.qq.tars.client.Communicator")
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
public class TarsTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TarsDtpAdapter tarsDtpAdapter() {
        return new TarsDtpAdapter();
    }
}
