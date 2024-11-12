/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.test.core.parse;

import cn.hutool.core.io.FileUtil;
import org.dromara.dynamictp.common.parser.config.PropertiesConfigParser;
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
        Assertions.assertEquals("dtpExecutor1", result.get("dynamictp.executors[0].threadPoolName").toString());
    }

}
