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

package org.dromara.dynamictp.common.util;

import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

/**
 * NacosUtil related
 *
 * @author yanhom
 * @since 1.0.0
 */
public final class NacosUtil {

    private NacosUtil() { }

    public static String deduceDataId(DtpProperties.Nacos nacos,
                                      Environment environment,
                                      ConfigFileTypeEnum configFileType) {
        String dataId;
        if (nacos != null && StringUtils.isNotBlank(nacos.getDataId())) {
            dataId = nacos.getDataId();
        } else {
            String[] profiles = environment.getActiveProfiles();
            if (profiles.length < 1) {
                profiles = environment.getDefaultProfiles();
            }

            String appName = environment.getProperty("spring.application.name");
            appName = StringUtils.isNoneBlank(appName) ? appName : "application";

            // default dataId style, for example: demo-dev
            dataId = appName + "-" + profiles[0] + "." + configFileType.getValue();
        }

        return dataId;
    }

    public static String getGroup(DtpProperties.Nacos nacos, String defaultGroup) {
        String group = defaultGroup;
        if (nacos != null && StringUtils.isNotBlank(nacos.getGroup())) {
            group = nacos.getGroup();
        }
        return group;
    }

    public static ConfigFileTypeEnum getConfigType(DtpProperties dtpProperties,
                                                   ConfigFileTypeEnum defaultType) {
        ConfigFileTypeEnum configFileType = defaultType;
        if (StringUtils.isNotBlank(dtpProperties.getConfigType())) {
            configFileType = ConfigFileTypeEnum.of(dtpProperties.getConfigType());
        }
        return configFileType;
    }
}
