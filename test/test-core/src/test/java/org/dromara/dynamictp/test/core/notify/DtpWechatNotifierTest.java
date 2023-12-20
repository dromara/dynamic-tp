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
import org.dromara.dynamictp.common.em.NotifyPlatformEnum;
import org.dromara.dynamictp.common.entity.MarkdownReq;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.util.DateUtil;
import org.dromara.dynamictp.common.util.JsonUtil;
import org.dromara.dynamictp.common.notifier.WechatNotifier;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class DtpWechatNotifierTest {


    @Test
    public void testWechatNotify() {
        WechatNotifier wechatNotifier = new WechatNotifier();
//        DtpWechatNotifier dtpWechatNotifier = new DtpWechatNotifier(wechatNotifier);
        NotifyPlatform notifyPlatform = new NotifyPlatform();
        notifyPlatform.setPlatformId("1");
        notifyPlatform.setPlatform(NotifyPlatformEnum.WECHAT.name().toLowerCase());
        // add your own wechat webhook url and secret
        notifyPlatform.setUrlKey("");
//        notifyPlatform.setSecret("");
        notifyPlatform.setReceivers("小红,小明");
        NotifyItem notifyItem = new NotifyItem();
        notifyItem.setReceivers("小红,小明");
        val url = WechatNotifyConst.WECHAT_WEB_HOOK + notifyPlatform.getUrlKey();
        val msgBody = buildMsgBody(notifyPlatform, buildContent(notifyItem, notifyPlatform));
        try {
            HttpResponse response = HttpRequest.post(url)
                    .setReadTimeout(1000)
                    .setConnectionTimeout(1000)
                    .body(msgBody).execute();
            System.out.println(response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
//        return Joiner.on(", @").join(receivers);
        return Arrays.stream(receivers).map(a -> "<@" + a + ">").collect(Collectors.joining(","));
    }

}
