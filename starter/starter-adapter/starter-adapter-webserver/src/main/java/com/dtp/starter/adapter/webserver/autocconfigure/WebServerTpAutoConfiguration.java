package com.dtp.starter.adapter.webserver.autocconfigure;

import com.dtp.adapter.webserver.WebServerEventService;
import com.dtp.adapter.webserver.handler.JettyDtpHandler;
import com.dtp.adapter.webserver.handler.TomcatDtpHandler;
import com.dtp.adapter.webserver.handler.UndertowDtpHandler;
import com.dtp.starter.common.autoconfigure.BaseBeanAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebServerTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter({BaseBeanAutoConfiguration.class})
public class WebServerTpAutoConfiguration {

    @Bean
    @ConditionalOnBean(name = {"tomcatServletWebServerFactory"})
    public TomcatDtpHandler tomcatTpHandler() {
        return new TomcatDtpHandler();
    }

    @Bean
    @ConditionalOnBean(name = {"JettyServletWebServerFactory"})
    public JettyDtpHandler jettyTpHandler() {
        return new JettyDtpHandler();
    }

    @Bean
    @ConditionalOnBean(name = {"undertowServletWebServerFactory"})
    public UndertowDtpHandler undertowTpHandler() {
        return new UndertowDtpHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebServerEventService webServerEventService() {
        return new WebServerEventService();
    }
}
