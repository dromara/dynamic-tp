/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.starter.adapter.webserver.autocconfigure;

import org.dromara.dynamictp.spring.DtpBaseBeanConfiguration;
import org.dromara.dynamictp.starter.adapter.webserver.autocconfigure.condition.ConditionalOnJettyWebServer;
import org.dromara.dynamictp.starter.adapter.webserver.autocconfigure.condition.ConditionalOnTomcatWebServer;
import org.dromara.dynamictp.starter.adapter.webserver.jetty.JettyDtpAdapter;
import org.dromara.dynamictp.starter.adapter.webserver.tomcat.TomcatDtpAdapter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebServerTpAutoConfiguration related.
 *
 * <p>Undertow support is removed on the Spring Boot 4 line (Servlet 6.1 baseline
 * dropped Undertow from Spring Boot itself).</p>
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
}
