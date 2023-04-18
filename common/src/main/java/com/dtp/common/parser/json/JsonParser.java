package com.dtp.common.parser.json;

import java.lang.reflect.Type;

/**
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
    boolean supports();

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
