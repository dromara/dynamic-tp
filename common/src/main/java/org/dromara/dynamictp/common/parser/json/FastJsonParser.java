package org.dromara.dynamictp.common.parser.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.lang.reflect.Type;

/**
 *
 * @author topsuder
 * @since 1.1.3
 */
public class FastJsonParser extends AbstractJsonParser {

    private static final String PACKAGE_NAME = "com.alibaba.fastjson.JSON";

    @Override
    public <T> T fromJson(String json, Type typeOfT) {
        return JSON.parseObject(json, typeOfT);
    }

    @Override
    public String toJson(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.DisableCircularReferenceDetect);
    }

    @Override
    protected String getMapperClassName() {
        return PACKAGE_NAME;
    }
}
