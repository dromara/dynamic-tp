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

package org.dromara.dynamictp.starter.adapter.dubbo.autoconfigure;

import org.dromara.dynamictp.adapter.dubbo.alibaba.AlibabaDubboDtpAdapter;
import org.dromara.dynamictp.spring.DtpBaseBeanConfiguration;
import org.dromara.dynamictp.starter.adapter.dubbo.autoconfigure.condition.ConditionOnAlibabaDubboApp;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AlibabaDubboTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Configuration
@ConditionOnAlibabaDubboApp
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@SuppressWarnings("all")
public class AlibabaDubboTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AlibabaDubboDtpAdapter alibabaDubboDtpHandler() {
        return new AlibabaDubboDtpAdapter();
    }
}
