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

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.constant.DingNotifyConst;
import org.dromara.dynamictp.common.em.NotifyPlatformEnum;
import org.dromara.dynamictp.common.entity.MarkdownReq;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.util.DingSignUtil;
import org.dromara.dynamictp.common.util.JsonUtil;

import java.util.List;
import java.util.Optional;

import static org.dromara.dynamictp.common.constant.DingNotifyConst.DING_NOTICE_TITLE;

/**
 * DingNotifier related
 *
 * @author yanhom
 * @author Kyao
 * @since 1.0.0
 **/
@Slf4j
public class DingNotifier extends AbstractHttpNotifier {

    private static final String ALL = "all";

    @Override
    public String platform() {
        return NotifyPlatformEnum.DING.name().toLowerCase();
    }

    @Override
    protected String buildMsgBody(NotifyPlatform platform, String content) {
        MarkdownReq.Markdown markdown = new MarkdownReq.Markdown();
        markdown.setTitle(DING_NOTICE_TITLE);
        markdown.setText(content);

        MarkdownReq.At at = new MarkdownReq.At();

        List<String> mobiles = Lists.newArrayList(platform.getReceivers().split(","));
        at.setAtMobiles(mobiles);
        if (mobiles.contains(ALL) || CollectionUtils.isEmpty(mobiles)) {
            at.setAtAll(true);
        }

        MarkdownReq markdownReq = new MarkdownReq();
        markdownReq.setMsgtype("markdown");
        markdownReq.setMarkdown(markdown);
        markdownReq.setAt(at);
        return JsonUtil.toJson(markdownReq);
    }

    @Override
    protected String buildUrl(NotifyPlatform platform) {
        String webHook = Optional.ofNullable(platform.getWebHook()).orElse(DingNotifyConst.DING_WEBHOOK);
        return getTargetUrl(platform.getSecret(), platform.getUrlKey(), webHook);
    }

    /**
     * Build target url.
     *
     * @param secret      secret
     * @param accessToken accessToken
     * @param webHook     webHook
     * @return url
     */
    private String getTargetUrl(String secret, String accessToken, String webHook) {
        if (StringUtils.isBlank(secret)) {
            return webHook + accessToken;
        }
        long timestamp = System.currentTimeMillis();
        String sign = DingSignUtil.dingSign(secret, timestamp);
        return webHook + accessToken + "&timestamp=" + timestamp + "&sign=" + sign;
    }
}
