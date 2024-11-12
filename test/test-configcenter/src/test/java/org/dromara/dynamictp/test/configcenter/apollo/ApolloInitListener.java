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

package org.dromara.dynamictp.test.configcenter.apollo;

import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.internals.YmlConfigFile;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.google.common.collect.Maps;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;
import java.util.Properties;

import static com.ctrip.framework.apollo.core.ConfigConsts.CONFIG_FILE_CONTENT_KEY;

/**
 * ApolloInitListener related
 *
 * @author yanhom
 * @since 1.1.0
 */
public class ApolloInitListener implements SpringApplicationRunListener {

    public ApolloInitListener(SpringApplication application, String[] args) {

    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> tmpMap = Maps.newHashMap();
        tmpMap.put(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES, "dynamic-tp-demo-dtp-dev.yml");
        propertySources.addFirst(new MapPropertySource("DtpRefreshTestPropertySource", tmpMap));

        YmlConfigFile configFile = (YmlConfigFile) ConfigService.getConfigFile("dynamic-tp-demo-dtp-dev", ConfigFileFormat.YML);
        Properties newProperties = new Properties();
        String content =
                "dynamictp:\n" +
                "      enabled: true                               # 是否启用 dynamictp，默认true\n" +
                "      executors:                                   # 动态线程池配置，都有默认值，采用默认值的可以不配置该项，减少配置量\n" +
                "        - threadPoolName: dtpExecutor1\n" +
                "          threadPoolAliasName: 测试线程池        # 线程池别名\n" +
                "          executorType: common                 # 线程池类型 common、eager、ordered、scheduled，默认 common\n" +
                "          corePoolSize: 6                      # 核心线程数，默认1\n" +
                "          maximumPoolSize: 8                   # 最大线程数，默认cpu核数\n" +
                "          queueCapacity: 2000                  # 队列容量，默认1024\n";
        newProperties.setProperty(CONFIG_FILE_CONTENT_KEY, content);
        configFile.onRepositoryChange("dynamic-tp-demo-dtp-dev.yml", newProperties);
    }
}
