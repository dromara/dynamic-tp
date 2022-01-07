package io.lyh.dtp.notify.ding;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * DingSignUtil related
 *
 * @author: yanhom1314@gmail.com
 * @date 2022-01-02 下午3:11
 */
@Slf4j
public class DingSignUtil {

    // The default encoding
    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    // Signature method
    private static final String ALGORITHM = "HmacSHA256";

    public static String dingSign(String secret, long timestamp) {
        String stringToSign = timestamp + "\n" + secret;
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(DEFAULT_ENCODING), ALGORITHM));
            byte[] signData = mac.doFinal(stringToSign.getBytes(DEFAULT_ENCODING));
            return URLEncoder.encode(new String(Base64.encodeBase64(signData)), DEFAULT_ENCODING.name());
        } catch (Exception e) {
            log.error("DynamicTp, cal sign error", e);
            return "";
        }
    }
}
