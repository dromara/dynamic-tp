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

package org.dromara.dynamictp.starter.cloud.huawei.autoconfigure;

import com.huaweicloud.common.configration.bootstrap.ConfigBootstrapProperties;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.spring.ex.DtpBaseBeanConfiguration;
import org.dromara.dynamictp.starter.cloud.huawei.refresher.CloudHuaweiRefresher;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author windsearcher
 */
@Configuration
@ConditionalOnClass(ConfigBootstrapProperties.class)
@ConditionalOnBean({DtpBaseBeanConfiguration.class})
@AutoConfigureAfter({DtpBaseBeanConfiguration.class})
public class DtpHuaweiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    @ConditionalOnProperty(value = "spring.cloud.servicecomb.config.enabled", matchIfMissing = true)
    public CloudHuaweiRefresher cloudHuaweiRefresher(DtpProperties dtpProperties) {
        return new CloudHuaweiRefresher(dtpProperties);
    }
}
