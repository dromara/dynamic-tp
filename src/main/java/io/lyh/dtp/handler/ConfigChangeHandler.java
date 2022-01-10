package io.lyh.dtp.handler;

import com.google.common.collect.Lists;
import io.lyh.dtp.common.em.ConfigFileTypeEnum;
import io.lyh.dtp.parser.ConfigParser;
import io.lyh.dtp.parser.PropertiesConfigParser;
import io.lyh.dtp.parser.YamlConfigParser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * ConfigChangeHandler related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class ConfigChangeHandler {

    private static final List<ConfigParser> PARSERS = Lists.newArrayList();

    private static class ConfigChangeHandlerHolder {
        private static final ConfigChangeHandler INSTANCE = new ConfigChangeHandler();
    }

    private ConfigChangeHandler() {
        ServiceLoader<ConfigParser> loader = ServiceLoader.load(ConfigParser.class);
        for (ConfigParser configParser : loader) {
            PARSERS.add(configParser);
        }

        PARSERS.add(new PropertiesConfigParser());
        PARSERS.add(new YamlConfigParser());
    }

    public static ConfigChangeHandler getInstance() {
        return ConfigChangeHandlerHolder.INSTANCE;
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
