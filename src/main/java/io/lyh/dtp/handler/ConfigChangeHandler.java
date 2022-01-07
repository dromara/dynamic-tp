package io.lyh.dtp.handler;

import io.lyh.dtp.common.em.ConfigFileTypeEnum;
import io.lyh.dtp.parser.ConfigParser;
import io.lyh.dtp.parser.PropertiesConfigParser;
import io.lyh.dtp.parser.YamlConfigParser;

import java.io.IOException;
import java.util.*;

/**
 * ConfigChangeHandler related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-29 18:06
 * @since 1.0.0
 **/
public class ConfigChangeHandler {

    private final List<ConfigParser> parsers;

    private static class ConfigChangeHandlerHolder {
        private static final ConfigChangeHandler INSTANCE = new ConfigChangeHandler();
    }

    private ConfigChangeHandler() {
        this.parsers = new LinkedList<>();
        ServiceLoader<ConfigParser> loader = ServiceLoader.load(ConfigParser.class);
        for (ConfigParser configParser : loader) {
            this.parsers.add(configParser);
        }

        this.parsers.add(new PropertiesConfigParser());
        this.parsers.add(new YamlConfigParser());
    }

    public static ConfigChangeHandler getInstance() {
        return ConfigChangeHandlerHolder.INSTANCE;
    }

    public Map<Object, Object> parseConfig(String content, ConfigFileTypeEnum type) throws IOException {
        for (ConfigParser parser : parsers) {
            if (parser.supports(type)) {
                return parser.doParse(content);
            }
        }

        return Collections.emptyMap();
    }
}
