package com.dtp.starter.common.autoconfigure;

import com.dtp.adapter.web.handler.JettyTpHandler;
import com.dtp.adapter.web.handler.TomcatTpHandler;
import com.dtp.adapter.web.handler.UndertowTpHandler;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.constant.DynamicTpConst;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebServerTpAutoConfiguration related
 *
 * @author yanhom
 */
@Configuration
@EnableConfigurationProperties(DtpProperties.class)
@ConditionalOnWebApplication
@ConditionalOnProperty(name = DynamicTpConst.DTP_ENABLED_PROP, matchIfMissing = true, havingValue = "true")
public class WebServerTpAutoConfiguration {

    @Bean
    @ConditionalOnBean(name = {"tomcatServletWebServerFactory"})
    public TomcatTpHandler tomcatTpHandler() {
        return new TomcatTpHandler();
    }

    @Bean
    @ConditionalOnBean(name = {"jettyServletWebServerFactory"})
    public JettyTpHandler jettyTpHandler() {
        return new JettyTpHandler();
    }

    @Bean
    @ConditionalOnBean(name = {"undertowServletWebServerFactory"})
    public UndertowTpHandler undertowTpHandler() {
        return new UndertowTpHandler();
    }
}
