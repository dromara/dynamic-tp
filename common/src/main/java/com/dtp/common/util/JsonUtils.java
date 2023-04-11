package com.dtp.common.util;

import com.dtp.common.ex.DtpException;
import com.dtp.common.json.parser.JsonParser;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * <p>JsonUtils 提供了将 Java 对象序列化为 JSON 字符串以及将 JSON 字符串反序列化为 Java 对象的方法。</p>
 * <p>JsonUtils 使用 SPI（Service Provider Interface）机制加载 JSON 序列化和反序列化器。</p>
 * <p>JsonUtils 可以自动检测并使用 Classpath 中可用的 JSON 序列化和反序列化器，包括 Jackson、Gson 和 FastJson。</p>
 * <p>如果在 Classpath 中找不到任何 JSON 序列化或反序列化器，则会抛出 DtpException 异常。</p>
 * <p>注意：如果您的应用程序使用了多个 JSON 序列化或反序列化器，您需要在使用 JsonUtils 之前设置默认序列化器或传递正确的序列化器。</p>
 *
 * @see com.dtp.common.json.parser.JsonParser

 * @author topsuder
 * @DATE 2023/4/11-14:11
 * @see com.dtp.common.util dynamic-tp
 */
@Slf4j
public final class JsonUtils {
    private static volatile JsonParser jsonParser;

    static {
        if (jsonParser == null) {
            synchronized (JsonUtils.class) {
                if (jsonParser == null) {
                    jsonParser = createJsonParser();
                }
            }
        }
    }

    private static JsonParser createJsonParser() {
        ServiceLoader<JsonParser> serviceLoader = ServiceLoader.load(JsonParser.class);
        Iterator<JsonParser> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            JsonParser jsonParser = iterator.next();
            if (jsonParser.isSupport()) {
                return jsonParser;
            }
        }
        throw new DtpException("No JSON parser found");
    }

    /**
     * 方法注释: <br>
     * 〈可用于将任何 Java 值序列化为字符串的方法。〉
     * @param	obj	任意类型入参
     * @return java.lang.String
     * @author topsuder 🌼🐇
     * @date 2023/4/11 19:43
     */
    public static String toJson(Object obj) {
        return jsonParser.toJson(obj);
    }
    /**
     * 方法注释: <br>
     * 〈此方法将指定的 Json 反序列化为指定类的对象。〉
     * @param	json 要反序列化的json字符串
     * @param	clazz T class
     * @return T
     * @author topsuder 🌼🐇
     * @date 2023/4/11 19:43
     */

    public static <T> T fromJson(String json, Class<T> clazz) {
        return jsonParser.fromJson(json, clazz);
    }


}
