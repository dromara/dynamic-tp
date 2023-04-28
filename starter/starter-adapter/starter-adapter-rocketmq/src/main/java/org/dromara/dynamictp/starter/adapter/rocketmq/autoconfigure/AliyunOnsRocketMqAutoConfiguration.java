package org.dromara.dynamictp.starter.adapter.rocketmq.autoconfigure;

import com.aliyun.openservices.ons.api.Consumer;
import org.dromara.dynamictp.adapter.rocketmq.AliyunOnsRocketMqAdapter;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Redick01
 */
@Configuration
@ConditionalOnClass(value = Consumer.class)
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class AliyunOnsRocketMqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliyunOnsRocketMqAdapter aliyunOnsRocketMqAdapter() {
        return new AliyunOnsRocketMqAdapter();
    }
}
