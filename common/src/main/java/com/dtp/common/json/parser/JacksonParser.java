package com.dtp.common.json.parser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

/**
 * @author topsuder
 * @see com.dtp.common.json.parser dynamic-tp
 */
@Slf4j
public class JacksonParser extends AbstractJsonParser<ObjectMapper> {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String PACKAGE_NAME = "com.fasterxml.jackson.databind.ObjectMapper";
    private volatile ObjectMapper mapper;

    public JacksonParser() {
        super();
    }

    @Override
    public <T> T fromJson(String json, Type typeOfT) {
        try {
            final ObjectMapper objectMapper = getMapper();
            return objectMapper.readValue(json, objectMapper.constructType(typeOfT));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toJson(Object obj) {
        try {
            return getMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private ObjectMapper getMapper() {
        // double check lock
        if (mapper == null) {
            synchronized (this) {
                if (mapper == null) {
                    mapper = createMapper();
                }
            }
        }
        return mapper;
    }

    protected ObjectMapper createMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        //configure方法 配置一些需要的参数
        // 转换为格式化的json 显示出来的格式美化
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        //序列化的时候序列对象的那些属性
        //JsonInclude.Include.NON_DEFAULT 属性为默认值不序列化
        //JsonInclude.Include.ALWAYS      所有属性
        //JsonInclude.Include.NON_EMPTY   属性为 空（“”） 或者为 NULL 都不序列化
        //JsonInclude.Include.NON_NULL    属性为NULL 不序列化
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);


        //反序列化时,遇到未知属性会不会报错
        //true - 遇到没有的属性就报错 false - 没有的属性不会管，不会报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //如果是空对象的时候,不抛异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);


        //修改序列化后日期格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
        return objectMapper;
    }


    @Override
    protected String getMapperClassName() {
        return PACKAGE_NAME;
    }
}
