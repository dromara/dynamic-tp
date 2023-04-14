package com.dtp.common.json.parser;

import java.lang.reflect.Type;

/**
 * @see com.dtp.common.json.parser dynamic-tp
 *
 * @author topsuder
 * @since 1.1.3
 */
public interface JsonParser {

    /**
     * Is support this json parser.
     *
     * @return true if support
     */
    boolean isSupport();

    /**
     * Json string to object.
     *
     * @param json json string
     * @param typeOfT type of target object
     * @return target object
     */
    <T> T fromJson(String json, Type typeOfT);

    /**
     * Object to json string.
     *
     * @param obj object
     * @return json string
     */
    String toJson(Object obj);

}
