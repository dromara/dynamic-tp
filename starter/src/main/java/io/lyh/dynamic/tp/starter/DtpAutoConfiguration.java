package io.lyh.dynamic.tp.starter;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import io.lyh.dynamic.tp.common.ApplicationContextHolder;
import io.lyh.dynamic.tp.common.DtpBannerPrinter;
import io.lyh.dynamic.tp.common.config.DtpProperties;
import io.lyh.dynamic.tp.common.constant.DynamicTpConst;
import io.lyh.dynamic.tp.core.DtpRegistry;
import io.lyh.dynamic.tp.core.monitor.DtpMonitor;
import io.lyh.dynamic.tp.core.monitor.endpoint.DtpEndpoint;
import io.lyh.dynamic.tp.core.refresh.apollo.ApolloRefresher;
import io.lyh.dynamic.tp.core.refresh.nacos.CloudNacosRefresher;
import io.lyh.dynamic.tp.core.refresh.nacos.NacosRefresher;
import io.lyh.dynamic.tp.core.support.DtpPostProcessor;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

import static com.ctrip.framework.apollo.spring.config.PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED;

/**
 * DtpAutoConfiguration related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Configuration
@ImportAutoConfiguration({DtpProperties.class})
@ConditionalOnProperty(prefix = DynamicTpConst.MAIN_PROPERTIES_PREFIX,
        value = "enabled", matchIfMissing = true, havingValue = "true")
public class DtpAutoConfiguration {

    @Resource
    private DtpProperties properties;

    @Bean
    public ApplicationContextHolder dtpApplicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    @ConditionalOnMissingBean
    public DtpBannerPrinter dtpBannerPrinter() {
        return new DtpBannerPrinter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DtpPostProcessor dtpPostProcessor() {
        return new DtpPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public DtpRegistry dtpRegistry() {
        return new DtpRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public DtpMonitor DtpMonitor() {
        return new DtpMonitor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    public DtpEndpoint dtpEndpoint() {
        return new DtpEndpoint();
    }

    @Configuration
    @ConditionalOnClass(NacosConfigProperties.class)
    protected static class SpringCloudNacosConfiguration {

        @Bean
        @ConditionalOnMissingBean()
        public CloudNacosRefresher cloudNacosRefresher() {
            return new CloudNacosRefresher();
        }
    }

    @Configuration
    @ConditionalOnClass(value = com.alibaba.nacos.api.config.ConfigService.class)
    @ConditionalOnMissingClass(value = {"com.alibaba.cloud.nacos.NacosConfigProperties"})
    protected static class NacosConfiguration {

        @Bean
        @ConditionalOnMissingBean()
        public NacosRefresher nacosRefresher() {
            return new NacosRefresher();
        }
    }

    @Configuration
    @ConditionalOnProperty(value = APOLLO_BOOTSTRAP_ENABLED, havingValue = "true")
    @ConditionalOnClass(com.ctrip.framework.apollo.ConfigService.class)
    protected static class ApolloConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ApolloRefresher apolloRefresher() {
            return new ApolloRefresher();
        }
    }
}