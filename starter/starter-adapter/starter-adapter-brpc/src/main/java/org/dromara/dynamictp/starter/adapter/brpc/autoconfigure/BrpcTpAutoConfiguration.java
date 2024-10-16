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

package org.dromara.dynamictp.starter.adapter.brpc.autoconfigure;

import org.dromara.dynamictp.apapter.brpc.client.StarlightClientDtpAdapter;
import org.dromara.dynamictp.apapter.brpc.server.StarlightServerDtpAdapter;
import org.dromara.dynamictp.spring.DtpBaseBeanConfiguration;
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
