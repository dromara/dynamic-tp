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

package org.dromara.dynamictp.start.extension.notify.yunzhijia.autoconfigure;

import org.dromara.dynamictp.core.notifier.DtpNotifier;
import org.dromara.dynamictp.extension.notify.yunzhijia.DtpYunZhiJiaNotifier;
import org.dromara.dynamictp.extension.notify.yunzhijia.YunZhiJiaNotifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * NotifyYunZhiJiaAutoConfiguration related
 *
 * @author husky12138
 * @since 1.1.4
 **/
@Configuration
public class NotifyYunZhiJiaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public YunZhiJiaNotifier yunZhiJiaNotifier() {
        return new YunZhiJiaNotifier();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(YunZhiJiaNotifier.class)
    public DtpNotifier dtpYunZhiJiaNotifier() {
        return new DtpYunZhiJiaNotifier();
    }

}
