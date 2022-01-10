package io.lyh.dtp.parser;

import io.lyh.dtp.common.em.ConfigFileTypeEnum;

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
}
