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
import org.dromara.dynamictp.starter.adapter.webserver.jetty.JettyDtpAdapter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Jetty web-server thread-pool adapter auto-configuration.
 *
 * <p>Class-level {@link ConditionalOnClass} is required so this configuration is
 * filtered out entirely when Jetty is not on the runtime classpath. Putting both
 * Tomcat and Jetty beans in one configuration class causes
 * {@code NoClassDefFoundError} when only one server is present.</p>
 *
 * @author yanhom
 * @since 1.0.6
 */
@AutoConfiguration(
        afterName = {
                "org.springframework.boot.jetty.autoconfigure.servlet.JettyServletWebServerAutoConfiguration",
                "org.springframework.boot.jetty.autoconfigure.reactive.JettyReactiveWebServerAutoConfiguration"
        },
        after = DtpBaseBeanConfiguration.class
)
@ConditionalOnClass(name = "org.springframework.boot.jetty.JettyWebServer")
@ConditionalOnWebApplication
@ConditionalOnBean(DtpBaseBeanConfiguration.class)
public class JettyWebServerTpAutoConfiguration {

    @Bean
    public JettyDtpAdapter jettyTpHandler() {
        return new JettyDtpAdapter();
    }
}
