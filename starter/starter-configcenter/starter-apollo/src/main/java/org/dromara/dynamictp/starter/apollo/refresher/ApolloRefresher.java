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
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.handler.ConfigHandler;
import org.dromara.dynamictp.spring.AbstractSpringRefresher;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.MAIN_PROPERTIES_PREFIX;

/**
 * ApolloRefresher related
 * <p>Listen for configuration file changes</p>
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class ApolloRefresher extends AbstractSpringRefresher implements InitializingBean, ConfigFileChangeListener {

    private static final Splitter NAMESPACE_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

    private final List<Pair<String, ConfigFileFormat>> namespacePairList = Lists.newArrayList();

    public ApolloRefresher(DtpProperties dtpProperties) {
        super(dtpProperties);
    }

    @Override
    public void onChange(ConfigFileChangeEvent changeEvent) {
        val configHandler = ConfigHandler.getInstance();
        val mergedProperties = Maps.newHashMap();
        namespacePairList.forEach(pair -> {
            String content;
            if (changeEvent.getNamespace().startsWith(pair.getLeft())) {
                content = changeEvent.getNewValue();
            } else {
                content = ConfigService.getConfigFile(pair.getLeft(), pair.getRight()).getContent();
            }
            if (StringUtils.isBlank(content)) {
                return;
            }
            try {
                Map<Object, Object> properties = configHandler.parseConfig(content, ConfigFileTypeEnum.of(pair.getRight().getValue()));
                if (MapUtils.isNotEmpty(properties)) {
                    mergedProperties.putAll(properties);
                }
            } catch (IOException e) {
                log.error("DynamicTp refresher, parse apollo config file error, namespace: {}", pair.getLeft(), e);
            }
        });
        refresh(mergedProperties);
    }

    @Override
    public void afterPropertiesSet() {
        List<String> namespaceList = getNamespaceList();
        log.info("Apollo bootstrap namespaces: {}", namespaceList);
        for (String namespace : namespaceList) {
            ConfigFileFormat format = determineFileFormat(namespace);
            String actualNamespaceName = trimNamespaceFormat(namespace, format);
            ConfigFile configFile = ConfigService.getConfigFile(actualNamespaceName, format);
            if (!isDtpNamespace(configFile.getContent(), ConfigFileTypeEnum.of(format.getValue()))) {
                continue;
            }
            try {
                namespacePairList.add(Pair.of(actualNamespaceName, format));
                configFile.addChangeListener(this);
                log.info("DynamicTp refresher, add listener success, namespace: {}", actualNamespaceName);
            } catch (Exception e) {
                log.error("DynamicTp refresher, add listener error, namespace: {}", actualNamespaceName, e);
            }
        }
    }

    private ConfigFileFormat determineFileFormat(String namespaceName) {
        String lowerCase = namespaceName.toLowerCase();
        for (ConfigFileFormat format : ConfigFileFormat.values()) {
            if (lowerCase.endsWith("." + format.getValue())) {
                return format;
            }
        }
        return ConfigFileFormat.Properties;
    }

    private String trimNamespaceFormat(String namespaceName, ConfigFileFormat format) {
        String extension = "." + format.getValue();
        if (!namespaceName.toLowerCase().endsWith(extension)) {
            return namespaceName;
        }
        return namespaceName.substring(0, namespaceName.length() - extension.length());
    }

    private List<String> getNamespaceList() {
        String namespaces = environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES,
                ConfigConsts.NAMESPACE_APPLICATION);
        return NAMESPACE_SPLITTER.splitToList(namespaces);
    }

    private boolean isDtpNamespace(String content, ConfigFileTypeEnum configFileType) {
        val configHandler = ConfigHandler.getInstance();
        try {
            val properties = configHandler.parseConfig(content, configFileType);
            return properties.keySet().stream().anyMatch(key -> key.toString().startsWith(MAIN_PROPERTIES_PREFIX));
        } catch (IOException e) {
            return false;
        }
    }
}
