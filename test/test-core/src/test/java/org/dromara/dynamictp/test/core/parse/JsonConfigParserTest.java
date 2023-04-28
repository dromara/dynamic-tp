package org.dromara.dynamictp.test.core.parse;

import cn.hutool.core.io.FileUtil;
import org.dromara.dynamictp.common.parser.config.JsonConfigParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JsonConfigParserTest related
 *
 * @author yanhom
 * @since 1.1.0
 */
public class JsonConfigParserTest {

    @Test
    void testDoParse() throws IOException {
        File file = ResourceUtils.getFile("classpath:demo-dtp-dev.json");
        String content = FileUtil.readString(file, StandardCharsets.UTF_8);

        JsonConfigParser parser = new JsonConfigParser();
        Map<Object, Object> result = parser.doParse(content);
        Assertions.assertEquals("dtpExecutor1", result.get("spring.dynamic.tp.executors[0].threadPoolName").toString());
    }

}
