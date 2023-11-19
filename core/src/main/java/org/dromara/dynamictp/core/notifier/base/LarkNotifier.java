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

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.constant.LarkNotifyConst;
import org.dromara.dynamictp.common.em.NotifyPlatformEnum;
import org.dromara.dynamictp.common.entity.NotifyPlatform;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.dromara.dynamictp.common.constant.LarkNotifyConst.SIGN_PARAM;
import static org.dromara.dynamictp.common.constant.LarkNotifyConst.SIGN_REPLACE;

/**
 * LarkNotifier
 *
 * @author fxbin
 * @version v1.0
 * @since 2022/4/28 23:25
 */
@Slf4j
public class LarkNotifier extends AbstractHttpNotifier {

    /**
     * LF
     */
    public static final String LF = "\n";

    /**
     * HmacSHA256 encryption algorithm
     */
    public static final String HMAC_SHA_256 = "HmacSHA256";

    @Override
    public String platform() {
        return NotifyPlatformEnum.LARK.name().toLowerCase();
    }

    /**
     * get signature
     *
     * @param secret    secret
     * @param timestamp timestamp
     * @return signature
     * @throws NoSuchAlgorithmException Mac.getInstance("HmacSHA256")
     * @throws InvalidKeyException      mac.init(java.security.Key)
     */
    protected String genSign(String secret, Long timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        String stringToSign = timestamp + LF + secret;
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
        byte[] signData = mac.doFinal(new byte[]{});
        return new String(Base64.encodeBase64(signData));
    }

    @Override
    protected String buildMsgBody(NotifyPlatform platform, String content) {
        if (StringUtils.isBlank(platform.getSecret())) {
            return content;
        }
        try {
            val secondsTimestamp = System.currentTimeMillis() / 1000;
            val sign = genSign(platform.getSecret(), secondsTimestamp);
            content = content.replace(SIGN_REPLACE, String.format(SIGN_PARAM, secondsTimestamp, sign));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("DynamicTp notify, lark generate signature failed...", e);
        }
        return content;
    }

    @Override
    protected String buildUrl(NotifyPlatform platform) {
        return LarkNotifyConst.LARK_WEBHOOK + platform.getUrlKey();
    }
}
