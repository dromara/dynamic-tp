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

package org.dromara.dynamictp.test.core.notify;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.constant.WechatNotifyConst;
import org.dromara.dynamictp.common.entity.MarkdownReq;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.util.DateUtil;
import org.dromara.dynamictp.common.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
class DtpWechatNotifierTest {

    @Test
    void testFormatReceivers() {
        String result = formatReceivers("小红,小明");
        assertEquals("<@小红>,<@小明>", result);
    }

    @Test
    void testFormatReceiversSingle() {
        String result = formatReceivers("admin");
        assertEquals("<@admin>", result);
    }

    @Test
    void testGetReceivesUsesNotifyItemFirst() {
        NotifyItem notifyItem = new NotifyItem();
        notifyItem.setReceivers("alice,bob");
        NotifyPlatform platform = new NotifyPlatform();
        platform.setReceivers("fallback");

        assertEquals("<@alice>,<@bob>", getReceives(notifyItem, platform));
    }

    @Test
    void testGetReceivesFallsBackToPlatform() {
        NotifyItem notifyItem = new NotifyItem();
        // notifyItem receivers is blank
        NotifyPlatform platform = new NotifyPlatform();
        platform.setReceivers("小红,小明");

        assertEquals("<@小红>,<@小明>", getReceives(notifyItem, platform));
    }

    @Test
    void testGetReceivesEmptyWhenBothBlank() {
        NotifyItem notifyItem = new NotifyItem();
        NotifyPlatform platform = new NotifyPlatform();
        platform.setReceivers(""); // clear default "all" value

        assertEquals(StringUtils.EMPTY, getReceives(notifyItem, platform));
    }

    @Test
    void testBuildMsgBodyContainsMarkdown() {
        NotifyItem notifyItem = new NotifyItem();
        notifyItem.setReceivers("小红");
        NotifyPlatform platform = new NotifyPlatform();
        platform.setReceivers("小红");

        String content = buildContent(notifyItem, platform);
        String msgBody = buildMsgBody(platform, content);

        assertTrue(msgBody.contains("\"msgtype\":\"markdown\""), "msgBody should contain msgtype=markdown");
        assertTrue(msgBody.contains("\"content\""), "msgBody should contain content field");
    }

    @Test
    void testBuildContentNotEmpty() {
        NotifyItem notifyItem = new NotifyItem();
        notifyItem.setReceivers("小红");
        NotifyPlatform platform = new NotifyPlatform();
        platform.setReceivers("小红");

        String content = buildContent(notifyItem, platform);

        assertFalse(StringUtils.isBlank(content), "content should not be blank");
        assertTrue(content.contains("<@小红>"), "content should contain @-mention for receivers");
    }

    protected String buildMsgBody(NotifyPlatform platform, String content) {
        MarkdownReq markdownReq = new MarkdownReq();
        markdownReq.setMsgtype("markdown");
        MarkdownReq.Markdown markdown = new MarkdownReq.Markdown();
        markdown.setContent(content);
        markdownReq.setMarkdown(markdown);
        return JsonUtil.toJson(markdownReq);
    }

    protected String buildContent(NotifyItem notifyItem, NotifyPlatform platform) {
        return String.format(
                WechatNotifyConst.WECHAT_CHANGE_NOTICE_TEMPLATE,
                "test",
                "127.0.0.1:8080",
                "env",
                "test",
                1, 2,
                3, 4,
                10, 20,
                2000, 2000,
                "null",
                200, 200,
                "", "",
                getReceives(notifyItem, platform),
                DateUtil.now());
    }

    protected String getReceives(NotifyItem notifyItem, NotifyPlatform platform) {
        String receives = StringUtils.isBlank(notifyItem.getReceivers()) ?
                platform.getReceivers() : notifyItem.getReceivers();
        if (StringUtils.isBlank(receives)) {
            return StringUtils.EMPTY;
        }
        return formatReceivers(receives);
    }

    protected String formatReceivers(String receives) {
        String[] receivers = StringUtils.split(receives, ',');
        return Arrays.stream(receivers).map(a -> "<@" + a + ">").collect(Collectors.joining(","));
    }

}
