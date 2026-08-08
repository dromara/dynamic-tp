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

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dromara.dynamictp.common.entity.NotifyPlatform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
        HttpURLConnection conn = null;
        try {
            URL targetUrl = new URL(url);
            if (platform.getProxyType() != Proxy.Type.DIRECT) {
                Proxy proxy = new Proxy(platform.getProxyType(),
                        new InetSocketAddress(platform.getProxyHost(), platform.getProxyPort()));
                conn = (HttpURLConnection) targetUrl.openConnection(proxy);
            } else {
                // no explicit proxy — respect JVM system proxy settings (-Dhttp.proxyHost etc.)
                conn = (HttpURLConnection) targetUrl.openConnection();
            }
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(platform.getTimeout());
            conn.setReadTimeout(platform.getTimeout());
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("User-Agent", "DynamicTp");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(msgBody.getBytes(StandardCharsets.UTF_8));
            }

            int respCode = conn.getResponseCode();
            String respBody = readResponseBody(conn, respCode);
            if (respCode >= 200 && respCode < 300) {
                log.info("DynamicTp notify, {} send success, response: {}, request: {}",
                        platform(), respBody, msgBody);
            } else {
                log.error("DynamicTp notify, {} send failed, http status: {}, response: {}, request: {}",
                        platform(), respCode, respBody, msgBody);
            }
        } catch (Exception e) {
            log.error("DynamicTp notify, {} send failed, request: {}", platform(), msgBody, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readResponseBody(HttpURLConnection conn, int code) throws IOException {
        InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
        if (is == null) {
            return "";
        }
        try (InputStream in = is) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toString(StandardCharsets.UTF_8.name());
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
