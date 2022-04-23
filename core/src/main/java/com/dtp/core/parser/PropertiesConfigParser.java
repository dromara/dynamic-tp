package com.dtp.core.parser;

import com.dtp.common.em.ConfigFileTypeEnum;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * PropertiesConfigParser related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class PropertiesConfigParser extends AbstractConfigParser {

    private static final List<ConfigFileTypeEnum> CONFIG_TYPE = Lists.newArrayList(ConfigFileTypeEnum.PROPERTIES);

    @Override
    public List<ConfigFileTypeEnum> type() {
        return CONFIG_TYPE;
    }

    @Override
    public Map<Object, Object> doParse(String content) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(content));
        return properties;
    }
}
