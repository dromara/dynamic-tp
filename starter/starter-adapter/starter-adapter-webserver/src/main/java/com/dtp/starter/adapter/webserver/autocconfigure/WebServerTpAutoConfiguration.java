package com.dtp.starter.adapter.webserver.autocconfigure;

import com.dtp.adapter.webserver.JettyDtpAdapter;
import com.dtp.adapter.webserver.TomcatDtpAdapter;
import com.dtp.adapter.webserver.UndertowDtpAdapter;
import com.dtp.core.spring.DtpBaseBeanConfiguration;
import com.dtp.starter.adapter.webserver.autocconfigure.condition.ConditionalOnJettyWebServer;
import com.dtp.starter.adapter.webserver.autocconfigure.condition.ConditionalOnTomcatWebServer;
import com.dtp.starter.adapter.webserver.autocconfigure.condition.ConditionalOnUndertowWebServer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class WebServerTpAutoConfiguration {

    @Bean
    @ConditionalOnTomcatWebServer
    public TomcatDtpAdapter tomcatTpHandler() {
        return new TomcatDtpAdapter();
    }

    @Bean
    @ConditionalOnJettyWebServer
    public JettyDtpAdapter jettyTpHandler() {
        return new JettyDtpAdapter();
    }

    @Bean
    @ConditionalOnUndertowWebServer
    public UndertowDtpAdapter undertowTpHandler() {
        return new UndertowDtpAdapter();
    }

}
