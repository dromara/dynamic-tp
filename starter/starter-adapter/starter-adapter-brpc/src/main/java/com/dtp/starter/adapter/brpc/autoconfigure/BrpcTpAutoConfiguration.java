package com.dtp.starter.adapter.brpc.autoconfigure;

import com.dtp.apapter.brpc.client.StarlightClientDtpAdapter;
import com.dtp.apapter.brpc.server.StarlightServerDtpAdapter;
import com.dtp.core.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BrpcTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Configuration
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
public class BrpcTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "com.baidu.cloud.starlight.springcloud.client.annotation.RpcProxy")
    public StarlightClientDtpAdapter starlightClientDtpAdapter() {
        return new StarlightClientDtpAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "starlight.server.enable")
    public StarlightServerDtpAdapter starlightServerDtpAdapter() {
        return new StarlightServerDtpAdapter();
    }
}
