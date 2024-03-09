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

package org.dromara.dynamictp.extension.notify.yunzhijia;

import cn.hutool.core.net.url.UrlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.notifier.AbstractHttpNotifier;
import org.dromara.dynamictp.common.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * YunZhiJiaNotifier related
 *
 * @author husky12138
 * @since 1.1.4
 **/
@Slf4j
public class YunZhiJiaNotifier extends AbstractHttpNotifier {

    @Override
    public String platform() {
        return YunZhiJiaNotifyConst.PLATFORM_NAME.toLowerCase();
    }

    @Override
    protected String buildMsgBody(NotifyPlatform platform, String content) {
        Map<String, Object> map = new HashMap<>();
        map.put("content", content);
        map.put("msgType", 0);
        return JsonUtil.toJson(map);
    }

    @Override
    protected String buildUrl(NotifyPlatform platform) {
        if (StringUtils.isBlank(platform.getUrlKey())) {
            return platform.getWebhook();
        }
        UrlBuilder builder = UrlBuilder.of(Optional.ofNullable(platform.getWebhook()).orElse(YunZhiJiaNotifyConst.WEB_HOOK));
        if (StringUtils.isBlank(builder.getQuery().get(YunZhiJiaNotifyConst.YZJ_TYPE_PARAM))) {
            builder.addQuery(YunZhiJiaNotifyConst.YZJ_TYPE_PARAM, 0);
        }
        if (StringUtils.isBlank(builder.getQuery().get(YunZhiJiaNotifyConst.YZJ_TOKEN_PARAM))) {
            builder.addQuery(YunZhiJiaNotifyConst.YZJ_TOKEN_PARAM, platform.getUrlKey());
        }
        return builder.build();
    }
}
