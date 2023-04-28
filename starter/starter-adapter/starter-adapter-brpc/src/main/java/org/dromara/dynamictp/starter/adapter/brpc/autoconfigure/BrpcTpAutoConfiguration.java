package org.dromara.dynamictp.starter.adapter.brpc.autoconfigure;

import org.dromara.dynamictp.apapter.brpc.client.StarlightClientDtpAdapter;
import org.dromara.dynamictp.apapter.brpc.server.StarlightServerDtpAdapter;
import org.dromara.dynamictp.core.spring.DtpBaseBeanConfiguration;
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
