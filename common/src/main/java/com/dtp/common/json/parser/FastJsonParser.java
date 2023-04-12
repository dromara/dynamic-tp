package com.dtp.common.json.parser;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Type;

/**
 * @see com.dtp.common.json.parser dynamic-tp
 *
 * @author topsuder
 * @since 1.1.3
 */
public class FastJsonParser extends AbstractJsonParser {

    private static final String PACKAGE_NAME = "com.alibaba.fastjson.JSON";

    public FastJsonParser() {
        super();
    }

    @Override
    public <T> T fromJson(String json, Type typeOfT) {
        return JSON.parseObject(json, typeOfT);
    }

    @Override
    public String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }

    @Override
    protected String getMapperClassName() {
        return PACKAGE_NAME;
    }
}
