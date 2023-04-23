package org.dromara.dynamictp.core.notifier.base;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.dromara.dynamictp.common.constant.LarkNotifyConst;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.em.NotifyPlatformEnum;
import org.dromara.dynamictp.common.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

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
public class LarkNotifier implements Notifier {

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
     * Execute real Lark send.
     *
     * @param notifyPlatform {@link NotifyPlatform}
     * @param text           send content
     */
    @Override
    public void send(NotifyPlatform notifyPlatform, String text) {
        String serverUrl = LarkNotifyConst.LARK_WEBHOOK + notifyPlatform.getUrlKey();
        if (StringUtils.isNotBlank(notifyPlatform.getSecret())) {
            try {
                val secondsTimestamp = TimeUtil.currentTimeSeconds();
                val sign = genSign(notifyPlatform.getSecret(), secondsTimestamp);
                text = text.replace(SIGN_REPLACE, String.format(SIGN_PARAM, secondsTimestamp, sign));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                log.error("DynamicTp notify, lark generate signature failed...", e);
            }
        }
        try {
            HttpResponse response = HttpRequest.post(serverUrl).body(text).execute();
            if (Objects.nonNull(response)) {
                log.info("DynamicTp notify, lark send success, response: {}, request:{}", response.body(), text);
            }
        } catch (Exception e) {
            log.error("DynamicTp notify, lark send failed...", e);
        }
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
}
