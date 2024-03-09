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

package org.dromara.dynamictp.common.notifier;

import cn.hutool.core.net.url.UrlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.constant.WechatNotifyConst;
import org.dromara.dynamictp.common.em.NotifyPlatformEnum;
import org.dromara.dynamictp.common.entity.MarkdownReq;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.util.JsonUtil;

import java.util.Optional;

/**
 * WechatNotifier related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class WechatNotifier extends AbstractHttpNotifier {

    @Override
    public String platform() {
        return NotifyPlatformEnum.WECHAT.name().toLowerCase();
    }

    @Override
    protected String buildMsgBody(NotifyPlatform platform, String content) {
        MarkdownReq markdownReq = new MarkdownReq();
        markdownReq.setMsgtype("markdown");
        MarkdownReq.Markdown markdown = new MarkdownReq.Markdown();
        markdown.setContent(content);
        markdownReq.setMarkdown(markdown);
        return JsonUtil.toJson(markdownReq);
    }

    @Override
    protected String buildUrl(NotifyPlatform platform) {
        if (StringUtils.isBlank(platform.getUrlKey())) {
            return platform.getWebhook();
        }
        UrlBuilder builder = UrlBuilder.of(Optional.ofNullable(platform.getWebhook()).orElse(WechatNotifyConst.WECHAT_WEB_HOOK));
        if (StringUtils.isBlank(builder.getQuery().get(WechatNotifyConst.KEY_PARAM))) {
            builder.addQuery(WechatNotifyConst.KEY_PARAM, platform.getUrlKey());
        }
        return builder.build();
    }
}
