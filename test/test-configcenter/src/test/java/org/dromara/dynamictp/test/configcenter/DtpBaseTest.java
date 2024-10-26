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

package org.dromara.dynamictp.test.configcenter;

import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.dromara.dynamictp.spring.support.YamlPropertySourceFactory;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * DtpBaseTest related
 *
 * @author yanhom
 * @since 1.1.7
 */
@EnableDynamicTp
@EnableAutoConfiguration
@SpringBootTest(classes = {DtpBaseTest.class})
@PropertySource(value = "classpath:/dynamic-tp-demo-dtp-dev.yml", factory = YamlPropertySourceFactory.class)
@ComponentScan(basePackages = {"org.dromara.dynamictp.test.configcenter", "org.dromara.dynamictp.spring"})
@AutoConfigureAfter(ConfigurationPropertiesRebinderAutoConfiguration.class)
public class DtpBaseTest {

    @Autowired
    protected ApplicationEventPublisher publisher;

    @Autowired
    protected ConfigurableEnvironment environment;

    protected static ConfigurableApplicationContext context;

    @BeforeAll
    public static void setUp() {
        context = SpringApplication.run(DtpBaseTest.class);
    }
}
