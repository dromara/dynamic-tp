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

package org.dromara.dynamictp.test.common.entity;

import com.google.common.collect.Lists;
import org.dromara.dynamictp.common.entity.MarkdownReq;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * MarkdownReqTest related.
 */
class MarkdownReqTest {

    @Test
    void testAccessors() {
        MarkdownReq req = new MarkdownReq();
        MarkdownReq.Markdown markdown = new MarkdownReq.Markdown();
        MarkdownReq.At at = new MarkdownReq.At();

        markdown.setTitle("title");
        markdown.setContent("content");
        markdown.setText("text");
        at.setAtMobiles(Lists.newArrayList("13800000000"));
        at.setIsAtAll(true);
        req.setMsgtype("markdown");
        req.setMarkdown(markdown);
        req.setAt(at);

        Assertions.assertEquals("markdown", req.getMsgtype());
        Assertions.assertSame(markdown, req.getMarkdown());
        Assertions.assertSame(at, req.getAt());
        Assertions.assertEquals("title", markdown.getTitle());
        Assertions.assertEquals("content", markdown.getContent());
        Assertions.assertEquals("text", markdown.getText());
        Assertions.assertEquals(Lists.newArrayList("13800000000"), at.getAtMobiles());
        Assertions.assertTrue(at.getIsAtAll());
    }
}
