package com.dtp.starter.adapter.rocketmq.autoconfigure;

import com.dtp.adapter.rocketmq.StreamRocketMqDtpAdapter;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * StreamRocketMqTpAutoConfiguration related
 *
 * @author zhanjb
 * @since 1.1.0
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.cloud.stream.rocketmq.binder", value = {"name-server"})
@ConditionalOnBean({BaseBeanAutoConfiguration.class})
@AutoConfigureAfter({BaseBeanAutoConfiguration.class})
@SuppressWarnings("all")
public class StreamRocketMqTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StreamRocketMqDtpAdapter rocketMqDtpHandler() {
        return new StreamRocketMqDtpAdapter();
    }
}
