package com.dtp.starter.adapter.grpc.autoconfigure;

import com.dtp.adapter.grpc.GrpcDtpAdapter;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
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
@AutoConfigureAfter({BaseBeanAutoConfiguration.class})
@SuppressWarnings("all")
public class GrpcTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GrpcDtpAdapter grpcDtpAdapter() {
        return new GrpcDtpAdapter();
    }
}
