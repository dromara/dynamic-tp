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

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dromara.dynamictp.common.entity.NotifyPlatform;

import java.util.Objects;

/**
 * The notification is sent over http
 *
 * @author kyao
 * @since 1.1.3
 */
@Slf4j
public abstract class AbstractHttpNotifier extends AbstractNotifier {

    @Override
    protected void send0(NotifyPlatform platform, String content) {
        val url = buildUrl(platform);
        val msgBody = buildMsgBody(platform, content);
        HttpRequest request = HttpRequest.post(url)
                .setConnectionTimeout(platform.getTimeout())
                .setReadTimeout(platform.getTimeout())
                .body(msgBody);
        HttpResponse response = request.execute();
        if (Objects.nonNull(response)) {
            log.info("DynamicTp notify, {} send success, response: {}, request: {}",
                    platform(), response.body(), msgBody);
        }
    }

    /**
     * build http message body
     * @param platform platform
     * @param content content
     * @return java.lang.String
     */
    protected abstract String buildMsgBody(NotifyPlatform platform, String content);

    /**
     * build http url
     * @param platform platform
     * @return java.lang.String
     */
    protected abstract String buildUrl(NotifyPlatform platform);

}
