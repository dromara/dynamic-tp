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

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.util.JsonUtil;
import org.dromara.dynamictp.core.notifier.base.Notifier;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * YunZhiJiaNotifier related
 *
 * @author husky12138
 * @since 1.0.0
 **/
@Slf4j
public class YunZhiJiaNotifier implements Notifier {

    @Override
    public String platform() {
        return YunZhiJiaNotifyConst.PLATFORM_NAME.toLowerCase();
    }

    /**
     * Execute real YunZhiJia send.
     *
     * @param platform send platform
     * @param text     send content
     */
    @Override
    public void send(NotifyPlatform platform, String text) {
        String serverUrl = YunZhiJiaNotifyConst.WEB_HOOK + platform.getUrlKey();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json;charset=UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("content", text);
        map.put("msgType", 0);
        try {
            HttpResponse response = HttpRequest.post(serverUrl).header(headers).body(JsonUtil.toJson(map)).execute();
            if (Objects.nonNull(response)) {
                log.info("DynamicTp notify, YunZhiJia send success, response: {}, request:{}",
                        response.body(), JsonUtil.toJson(map));
            }
        } catch (Exception e) {
            log.error("DynamicTp notify, YunZhiJia send failed...", e);
        }
    }
}
