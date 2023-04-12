package com.dtp.common.json.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * @see com.dtp.common.json.parser dynamic-tp
 *
 * @author topsuder
 * @since 1.1.3
 */
public class GsonParser extends AbstractJsonParser {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String PACKAGE_NAME = "com.google.gson.Gson";
    private volatile Gson mapper;

    @Override
    public <T> T fromJson(String json, Type typeOfT) {
        return getMapper().fromJson(json, typeOfT);
    }

    @Override
    public String toJson(Object obj) {
        return getMapper().toJson(obj);
    }

    private Gson getMapper() {
        if (mapper == null) {
            synchronized (this) {
                if (mapper == null) {
                    mapper = createMapper();
                }
            }
        }
        return mapper;
    }

    protected Gson createMapper() {
        return new GsonBuilder()
                .setDateFormat(DATE_FORMAT)
                .create();
    }

    @Override
    protected String getMapperClassName() {
        return PACKAGE_NAME;
    }
}
