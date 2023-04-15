package com.dtp.common.parser.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author topsuder
 * @since 1.1.3
 */
@Slf4j
public class JacksonParser extends AbstractJsonParser {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String PACKAGE_NAME = "com.fasterxml.jackson.databind.ObjectMapper";
    private volatile ObjectMapper mapper;

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
        // 只提供最简单的方案
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return JsonMapper.builder()
                .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
                // 反序列化时,遇到未知属性会不会报错 true - 遇到没有的属性就报错 false - 没有的属性不会管，不会报错
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // 如果是空对象的时候,不抛异常
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                // 序列化的时候序列对象的那些属性
                .serializationInclusion(JsonInclude.Include.NON_EMPTY)
                .addModules(javaTimeModule)
                .addModules(new JavaTimeModule())
                // 修改序列化后日期格式
                .defaultDateFormat(new SimpleDateFormat(DATE_FORMAT))
                .build();
    }

    @Override
    protected String getMapperClassName() {
        return PACKAGE_NAME;
    }
}
