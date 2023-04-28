package org.dromara.dynamictp.starter.adapter.motan.autoconfigure;

import org.dromara.dynamictp.adapter.motan.MotanDtpAdapter;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MotanTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Configuration
@ConditionalOnClass(name = "com.weibo.api.motan.config.springsupport.ServiceConfigBean")
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class MotanTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MotanDtpAdapter motanDtpAdapter() {
        return new MotanDtpAdapter();
    }
}
