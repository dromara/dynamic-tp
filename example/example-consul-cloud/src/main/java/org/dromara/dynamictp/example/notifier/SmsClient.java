package org.dromara.dynamictp.example.notifier;

import lombok.extern.slf4j.Slf4j;

/**
 * SmsClient related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
public class SmsClient {

    public void send(String secret, String[] receivers, String content) {
        log.info("send sms, secret: {}, receivers: {}, content: {}", secret, receivers, content);
    }
}
