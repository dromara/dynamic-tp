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

package org.dromara.dynamictp.starter.adapter.tars.autoconfigure;

import org.dromara.dynamictp.adapter.tars.TarsDtpAdapter;
import org.dromara.dynamictp.spring.DtpBaseBeanConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TarsTpAutoConfiguration related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Configuration
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
@ConditionalOnClass(name = "com.qq.tars.client.Communicator")
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
public class TarsTpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TarsDtpAdapter tarsDtpAdapter() {
        return new TarsDtpAdapter();
    }
}
