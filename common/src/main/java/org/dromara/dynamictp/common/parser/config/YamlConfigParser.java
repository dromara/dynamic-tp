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

package org.dromara.dynamictp.common.parser.config;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

/**
 * YamlConfigParser related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class YamlConfigParser extends AbstractConfigParser {

    private static final List<ConfigFileTypeEnum> CONFIG_TYPES = Lists.newArrayList(
            ConfigFileTypeEnum.YML, ConfigFileTypeEnum.YAML);

    @Override
    public List<ConfigFileTypeEnum> types() {
        return CONFIG_TYPES;
    }

    @Override
    public Map<Object, Object> doParse(String content) {

        if (StringUtils.isEmpty(content)) {
            return Collections.emptyMap();
        }
        content=setGlobalExecutor(content);
        YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
        bean.setResources(new ByteArrayResource(content.getBytes()));
        return bean.getObject();
    }
    private String setGlobalExecutor(String content) {
        Yaml yaml = new Yaml();
        Map<String, Map<String,Map<String,Map<String,Object>>>> dtpProperties = yaml.load(content);
        Map<String, Object> globalSettings=(Map<String, Object>) dtpProperties.get("spring").get("dynamic").get("tp").get("executorsGlobal");
        List<Map<String, Object>> executors = (List<Map<String, Object>>) dtpProperties.get("spring").get("dynamic").get("tp").get("executors");
        executors.forEach(executor ->{
            mergeSettingsWithoutOverwrite(globalSettings, executor);
        });
        return yaml.dump(dtpProperties);
    }
    private static void mergeSettingsWithoutOverwrite(Map<String, Object> globalSettings, Map<String, Object> object) {
        for (Map.Entry<String, Object> entry : globalSettings.entrySet()) {
            if (!object.containsKey(entry.getKey())) {
                object.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
