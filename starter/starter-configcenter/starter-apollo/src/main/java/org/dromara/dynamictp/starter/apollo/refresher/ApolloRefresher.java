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

package org.dromara.dynamictp.starter.apollo.refresher;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.refresher.AbstractRefresher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

/**
 * ApolloRefresher related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class ApolloRefresher extends AbstractRefresher implements ConfigFileChangeListener, InitializingBean {

    @Value("${apollo.bootstrap.namespaces:application}")
    private String namespace;

    private ConfigFileTypeEnum configFileType;

    public ApolloRefresher(DtpProperties dtpProperties) {
        super(dtpProperties);
    }

    @Override
    public void afterPropertiesSet() {

        String[] apolloNamespaces = this.namespace.split(",");
        String realNamespace = apolloNamespaces[0];
        DtpProperties.Apollo apollo = dtpProperties.getApollo();
        if (apollo != null && StringUtils.isNotBlank(apollo.getNamespace())) {
            realNamespace = apollo.getNamespace();
        }

        configFileType = deduceFileType(realNamespace);
        namespace = realNamespace.replaceAll("." + configFileType.getValue(), "");
        ConfigFileFormat configFileFormat = ConfigFileFormat.fromString(configFileType.getValue());
        ConfigFile configFile = ConfigService.getConfigFile(namespace, configFileFormat);
        try {
            configFile.addChangeListener(this);
            log.info("DynamicTp refresher, add listener success, namespace: {}", realNamespace);
        } catch (Exception e) {
            log.error("DynamicTp refresher, add listener error, namespace: {}", realNamespace, e);
        }
    }

    @Override
    public void onChange(ConfigFileChangeEvent changeEvent) {
        String content = changeEvent.getNewValue();
        refresh(content, configFileType);
    }

    private ConfigFileTypeEnum deduceFileType(String namespace) {
        ConfigFileTypeEnum configFileFormat = ConfigFileTypeEnum.PROPERTIES;
        if (namespace.contains(ConfigFileTypeEnum.YAML.getValue())) {
            configFileFormat = ConfigFileTypeEnum.YAML;
        } else if (namespace.contains(ConfigFileTypeEnum.YML.getValue())) {
            configFileFormat = ConfigFileTypeEnum.YML;
        } else if (namespace.contains(ConfigFileTypeEnum.JSON.getValue())) {
            configFileFormat = ConfigFileTypeEnum.JSON;
        } else if (namespace.contains(ConfigFileTypeEnum.XML.getValue())) {
            configFileFormat = ConfigFileTypeEnum.XML;
        } else if (namespace.contains(ConfigFileTypeEnum.TXT.getValue())) {
            configFileFormat = ConfigFileTypeEnum.TXT;
        }

        return configFileFormat;
    }
}
