package org.dromara.dynamictp.test.core.parse;

import cn.hutool.core.io.FileUtil;
import org.dromara.dynamictp.common.parser.config.YamlConfigParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * YamlConfigParserTest related
 *
 * @author yanhom
 * @since 1.1.0
 */
class YamlConfigParserTest {

    @Test
    void testDoParse() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:demo-dtp-dev.yml");
        String content = FileUtil.readString(file, StandardCharsets.UTF_8);

        YamlConfigParser parser = new YamlConfigParser();
        Map<Object, Object> result = parser.doParse(content);
        Assertions.assertEquals("dtpExecutor1", result.get("spring.dynamic.tp.executors[0].threadPoolName").toString());
    }
}
