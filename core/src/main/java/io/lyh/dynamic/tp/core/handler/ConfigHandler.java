package io.lyh.dynamic.tp.core.handler;

import com.google.common.collect.Lists;
import io.lyh.dynamic.tp.common.em.ConfigFileTypeEnum;
import io.lyh.dynamic.tp.core.parser.ConfigParser;
import io.lyh.dynamic.tp.core.parser.PropertiesConfigParser;
import io.lyh.dynamic.tp.core.parser.YamlConfigParser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * ConfigHandler related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class ConfigHandler {

    private static final List<ConfigParser> PARSERS = Lists.newArrayList();

    private static class ConfigHandlerHolder {
        private static final ConfigHandler INSTANCE = new ConfigHandler();
    }

    private ConfigHandler() {
        ServiceLoader<ConfigParser> loader = ServiceLoader.load(ConfigParser.class);
        for (ConfigParser configParser : loader) {
            PARSERS.add(configParser);
        }

        PARSERS.add(new PropertiesConfigParser());
        PARSERS.add(new YamlConfigParser());
    }

    public static ConfigHandler getInstance() {
        return ConfigHandlerHolder.INSTANCE;
    }

    public Map<Object, Object> parseConfig(String content, ConfigFileTypeEnum type) throws IOException {
        for (ConfigParser parser : PARSERS) {
            if (parser.supports(type)) {
                return parser.doParse(content);
            }
        }

        return Collections.emptyMap();
    }
}
