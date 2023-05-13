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

package org.dromara.dynamictp.core.notifier.base;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.dromara.dynamictp.common.constant.WechatNotifyConst;
import org.dromara.dynamictp.common.entity.MarkdownReq;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.em.NotifyPlatformEnum;
import org.dromara.dynamictp.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * WechatNotifier related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class WechatNotifier implements Notifier {

    @Override
    public String platform() {
        return NotifyPlatformEnum.WECHAT.name().toLowerCase();
    }

    /**
     * Execute real wechat send.
     *
     * @param platform send platform
     * @param text send content
     */
    @Override
    public void send(NotifyPlatform platform, String text) {
        String serverUrl = WechatNotifyConst.WECHAT_WEH_HOOK + platform.getUrlKey();
        MarkdownReq markdownReq = new MarkdownReq();
        markdownReq.setMsgtype("markdown");
        MarkdownReq.Markdown markdown = new MarkdownReq.Markdown();
        markdown.setContent(text);
        markdownReq.setMarkdown(markdown);

        try {
            HttpResponse response = HttpRequest.post(serverUrl).body(JsonUtil.toJson(markdownReq)).execute();
            if (Objects.nonNull(response)) {
                log.info("DynamicTp notify, wechat send success, response: {}, request:{}",
                        response.body(), JsonUtil.toJson(markdownReq));
            }
        } catch (Exception e) {
            log.error("DynamicTp notify, wechat send failed...", e);
        }
    }
}
