package org.dromara.dynamictp.starter.adapter.grpc.autoconfigure;

import org.dromara.dynamictp.adapter.grpc.GrpcDtpAdapter;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GrpcTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.9
 */
@Configuration
@ConditionalOnProperty(prefix = "grpc.server", value = {"port"})
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@SuppressWarnings("all")
public class GrpcTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GrpcDtpAdapter grpcDtpAdapter() {
        return new GrpcDtpAdapter();
    }
}
