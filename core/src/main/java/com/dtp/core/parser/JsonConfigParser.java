package com.dtp.core.parser;

import cn.hutool.core.map.MapUtil;
import com.dtp.common.em.ConfigFileTypeEnum;
import com.dtp.common.util.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

import static com.dtp.common.constant.DynamicTpConst.*;

/**
 * JsonConfigParser related
 *
 * @author: yanhom
 * @since 1.0.5
 **/
@Slf4j
public class JsonConfigParser extends AbstractConfigParser {

    private static final List<ConfigFileTypeEnum> CONFIG_TYPE = Lists.newArrayList(ConfigFileTypeEnum.JSON);

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
    }

    @Override
    public List<ConfigFileTypeEnum> type() {
        return CONFIG_TYPE;
    }

    @Override
    public Map<Object, Object> doParse(String content) {
        return doParse(content, MAIN_PROPERTIES_PREFIX);
    }

    @Override
    public Map<Object, Object> doParse(String content, String prefix) {

        Map<String, Object> originMap;
        try {
            originMap = MAPPER.readValue(content, LinkedHashMap.class);
        } catch (IOException e) {
            log.error("read value error", e);
            return Maps.newHashMapWithExpectedSize(0);
        }

        Map<Object, Object> result = Maps.newHashMap();

        flatMap(result, originMap, prefix);
        return result;
    }

    private void flatMap(Map<Object, Object> result, Map<String, Object> dataMap, String prefix) {

        if (MapUtil.isEmpty(dataMap)) {
            return;
        }

        dataMap.forEach((k, v) -> {
            String fullKey = genFullKey(prefix, k);
            if (v instanceof Map) {
                flatMap(result, (Map<String, Object>) v, fullKey);
                return;
            } else if (v instanceof Collection) {
                int count = 0;
                for (Object obj : (Collection<Object>) v) {
                    String kk = ARR_LEFT_BRACKET + (count++) + ARR_RIGHT_BRACKET;
                    flatMap(result, Collections.singletonMap(kk, obj), fullKey);
                }
                return;
            }

            result.put(fullKey, v);
        });
    }

    private String genFullKey(String prefix, String key) {
        if (StringUtil.isEmpty(prefix)) {
            return key;
        }

        return key.startsWith(ARR_LEFT_BRACKET) ? prefix.concat(key) : prefix.concat(DOT).concat(key);
    }
}
