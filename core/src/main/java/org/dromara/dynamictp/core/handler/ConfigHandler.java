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

package org.dromara.dynamictp.core.handler;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.dromara.dynamictp.common.parser.config.ConfigParser;
import org.dromara.dynamictp.common.parser.config.JsonConfigParser;
import org.dromara.dynamictp.common.parser.config.PropertiesConfigParser;
import org.dromara.dynamictp.common.parser.config.YamlConfigParser;
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ConfigHandler related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public final class ConfigHandler {

    private static final List<ConfigParser> PARSERS = Lists.newArrayList();

    private ConfigHandler() {
        List<ConfigParser> loadedParses = ExtensionServiceLoader.get(ConfigParser.class);
        if (CollectionUtils.isNotEmpty(loadedParses)) {
            PARSERS.addAll(loadedParses);
        }
        PARSERS.add(new PropertiesConfigParser());
        PARSERS.add(new YamlConfigParser());
        PARSERS.add(new JsonConfigParser());
    }

    public Map<Object, Object> parseConfig(String content, ConfigFileTypeEnum type) throws IOException {
        for (ConfigParser parser : PARSERS) {
            if (parser.supports(type)) {
                return parser.doParse(content);
            }
        }

        return Collections.emptyMap();
    }

    public static ConfigHandler getInstance() {
        return ConfigHandlerHolder.INSTANCE;
    }

    private static class ConfigHandlerHolder {
        private static final ConfigHandler INSTANCE = new ConfigHandler();
    }
}
