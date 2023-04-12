package com.dtp.common.json.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author topsuder
 * @see com.dtp.common.json.parser dynamic-tp
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
        TypeAdapter<LocalDateTime> timeTypeAdapter = new TypeAdapter<LocalDateTime>() {
            @Override
            public void write(JsonWriter out, LocalDateTime value) throws IOException {
                if (Objects.nonNull(value)) {
                    out.value(value.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
                } else {
                    out.nullValue();
                }
            }

            @Override
            public LocalDateTime read(JsonReader in) throws IOException {
                final JsonToken token = in.peek();
                if (token == JsonToken.NULL) {
                    return null;
                } else {
                    return LocalDateTime.parse(in.nextString(), DateTimeFormatter.ofPattern(DATE_FORMAT));
                }
            }
        };

        return new GsonBuilder()
                .setDateFormat(DATE_FORMAT)
                .registerTypeAdapter(LocalDateTime.class, timeTypeAdapter)
                .create();
    }

    @Override
    protected String getMapperClassName() {
        return PACKAGE_NAME;
    }
}
