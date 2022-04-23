package com.dtp.core.parser;

import com.dtp.common.em.ConfigFileTypeEnum;

import java.util.Map;

/**
 * AbstractConfigParser related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public abstract class AbstractConfigParser implements ConfigParser {

    @Override
    public boolean supports(ConfigFileTypeEnum type) {
        return this.type().contains(type);
    }

    @Override
    public Map<Object, Object> doParse(String content, String prefix) {
        throw new UnsupportedOperationException();
    }
}
