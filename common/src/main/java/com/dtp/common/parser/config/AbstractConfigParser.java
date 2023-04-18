package com.dtp.common.parser.config;

import com.dtp.common.em.ConfigFileTypeEnum;

import java.io.IOException;
import java.util.Map;

/**
 * AbstractConfigParser related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public abstract class AbstractConfigParser implements ConfigParser {

    @Override
    public boolean supports(ConfigFileTypeEnum type) {
        return this.types().contains(type);
    }

    @Override
    public Map<Object, Object> doParse(String content, String prefix) throws IOException {
        return doParse(content);
    }
}
