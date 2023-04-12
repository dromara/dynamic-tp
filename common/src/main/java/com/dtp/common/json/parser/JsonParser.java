package com.dtp.common.json.parser;

import java.lang.reflect.Type;

/**
 * @author topsuder
 * @see com.dtp.common.json.parser dynamic-tp
 */
public interface JsonParser {

    boolean isSupport();

    <T> T fromJson(String json, Type typeOfT);

    String toJson(Object obj);

}
