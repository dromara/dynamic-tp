package com.dtp.common.json.parser;

import com.dtp.common.ex.DtpException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
/**
 * @author topsuder
 * @DATE 2023/4/11-14:39
 * @Description
 * @see com.dtp.common.json.parser dynamic-tp
 */
@Slf4j
public class JacksonParser extends AbstractJsonParser<ObjectMapper> {

    public JacksonParser() {
        super();
    }

    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return getMapper().readValue(json, clazz);
        } catch (IOException e) {
            throw new DtpException(e.getMessage());
        }
    }

    @Override
    public String toJson(Object obj) {
        try {
            return getMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new DtpException(e.getMessage());
        }
    }

    @Override
    protected ObjectMapper createMapper() {
        return new ObjectMapper();
    }


    @Override
    protected String getMapperClassName() {
        return "com.fasterxml.jackson.databind.ObjectMapper";
    }
}
