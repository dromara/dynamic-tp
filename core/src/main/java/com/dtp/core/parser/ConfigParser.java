package com.dtp.core.parser;

import com.dtp.common.em.ConfigFileTypeEnum;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ConfigParser related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public interface ConfigParser {

    /**
     * Judge type.
     * @param type config file type
     * @return true if the parse supports this type, else false
     */
    boolean supports(ConfigFileTypeEnum type);

    /**
     * Type: yaml, properties...
     * @return the parse supports types.
     */
    List<ConfigFileTypeEnum> type();

    /**
     * Parse content.
     * @param content content
     * @return k-v properties
     * @throws IOException if occurs error while parsing
     */
    Map<Object, Object> doParse(String content) throws IOException;

    /**
     * Parse content.
     * @param content content
     * @param prefix key prefix
     * @return k-v properties
     */
    Map<Object, Object> doParse(String content, String prefix);
}
