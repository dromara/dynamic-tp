package com.dtp.common.parser.config;

import com.dtp.common.em.ConfigFileTypeEnum;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * PropertiesConfigParser related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class PropertiesConfigParser extends AbstractConfigParser {

    private static final List<ConfigFileTypeEnum> CONFIG_TYPES = Lists.newArrayList(ConfigFileTypeEnum.PROPERTIES);

    @Override
    public List<ConfigFileTypeEnum> types() {
        return CONFIG_TYPES;
    }

    @Override
    public Map<Object, Object> doParse(String content) throws IOException {

        if (StringUtils.isBlank(content)) {
            return Collections.emptyMap();
        }
        Properties properties = new Properties();
        properties.load(new StringReader(content));
        return properties;
    }
}
