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

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.yaml.snakeyaml.Yaml;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * YamlConfigParser related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class YamlConfigParser extends AbstractConfigParser {

    private static final List<ConfigFileTypeEnum> CONFIG_TYPES = Arrays.asList(
            ConfigFileTypeEnum.YML, ConfigFileTypeEnum.YAML);

    @Override
    public List<ConfigFileTypeEnum> types() {
        return CONFIG_TYPES;
    }

    @Override
    public Map<Object, Object> doParse(String content) {
        if (StringUtils.isBlank(content)) {
            return Collections.emptyMap();
        }

        Yaml yaml = new Yaml();
        Map<Object, Object> loadedYaml = yaml.load(content);

        if (MapUtils.isEmpty(loadedYaml)) {
            return Collections.emptyMap();
        }

        Map<Object, Object> flattenedMap = new LinkedHashMap<>();
        flattenMap(flattenedMap, loadedYaml, null);
        return flattenedMap;
    }

    @SuppressWarnings("unchecked")
    private void flattenMap(Map<Object, Object> result, Map<Object, Object> source, String path) {
        source.forEach((key, value) -> {
            String fullPath = (path != null ? path + "." + key : key.toString());
            if (value instanceof Map) {
                flattenMap(result, (Map<Object, Object>) value, fullPath);
            } else if (value instanceof List) {
                for (int i = 0; i < ((List<?>) value).size(); i++) {
                    flattenMap(result, Collections.singletonMap("[" + i + "]", ((List<?>) value).get(i)), fullPath);
                }
            } else {
                fullPath = fullPath.replaceAll("\\.\\[", "[");
                result.put(fullPath, value != null ? value.toString() : null);
            }
        });
    }
}
