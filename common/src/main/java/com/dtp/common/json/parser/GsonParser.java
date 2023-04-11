package com.dtp.common.json.parser;

import com.google.gson.Gson;
/**
 * @author topsuder
 * @DATE 2023/4/11-14:39
 * @Description
 * @see com.dtp.common.json.parser dynamic-tp
 */
public class GsonParser extends AbstractJsonParser<Gson> {
    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        return getMapper().fromJson(json, clazz);
    }

    @Override
    public String toJson(Object obj) {
        return getMapper().toJson(obj);
    }


    @Override
    protected Gson createMapper() {
        return new Gson();
    }

    @Override
    protected String getMapperClassName() {
        return "com.google.gson.Gson";
    }
}