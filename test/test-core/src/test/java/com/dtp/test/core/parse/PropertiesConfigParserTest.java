package com.dtp.test.core.parse;

import cn.hutool.core.io.FileUtil;
import com.dtp.common.parser.config.PropertiesConfigParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * PropertiesConfigParserTest related
 *
 * @author yanhom
 * @since 1.1.0
 */
public class PropertiesConfigParserTest {

    @Test
    void testDoParse() throws IOException {
        File file = ResourceUtils.getFile("classpath:demo-dtp-dev.properties");
        String content = FileUtil.readString(file, StandardCharsets.UTF_8);

        PropertiesConfigParser parser = new PropertiesConfigParser();
        Map<Object, Object> result = parser.doParse(content);
        Assertions.assertEquals("dtpExecutor1", result.get("spring.dynamic.tp.executors[0].threadPoolName").toString());
    }

}
