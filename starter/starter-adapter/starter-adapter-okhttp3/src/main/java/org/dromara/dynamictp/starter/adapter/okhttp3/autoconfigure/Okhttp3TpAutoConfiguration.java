package org.dromara.dynamictp.starter.adapter.okhttp3.autoconfigure;

import org.dromara.dynamictp.adapter.okhttp3.Okhttp3DtpAdapter;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
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
