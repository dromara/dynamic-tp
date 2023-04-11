package com.dtp.common.json.parser;

import com.alibaba.fastjson.JSON;

/**
 * @author topsuder
 * @DATE 2023/4/11-14:39
 * @see com.dtp.common.json.parser dynamic-tp
 */
public class FastJsonParser extends AbstractJsonParser {

    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    @Override
    public String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }

    @Override
    protected boolean skipMapper() {
        return true;
    }

    @Override
    protected String getMapperClassName() {
        return "com.alibaba.fastjson.JSON";
    }
}