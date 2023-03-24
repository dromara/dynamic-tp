package com.dtp.starter.adapter.okhttp3.autoconfigure;

import com.dtp.adapter.okhttp3.Okhttp3DtpAdapter;
import com.dtp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Okhttp3TpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Configuration
@ConditionalOnClass(name = "okhttp3.OkHttpClient")
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class Okhttp3TpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Okhttp3DtpAdapter okhttp3DtpAdapter() {
        return new Okhttp3DtpAdapter();
    }
}
