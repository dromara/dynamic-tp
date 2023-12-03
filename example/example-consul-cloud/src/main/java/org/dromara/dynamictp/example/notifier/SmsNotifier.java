package org.dromara.dynamictp.example.notifier;

import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.core.notifier.base.AbstractNotifier;

/**
 * SmsNotifier related
 *
 * @author yanhom
 * @since 1.1.0
 */
public class SmsNotifier extends AbstractNotifier {

    private final SmsClient smsClient;

    public SmsNotifier(SmsClient smsClient) {
        this.smsClient = smsClient;
    }

    @Override
    public String platform() {
        return "sms";
    }

    @Override
    protected void send0(NotifyPlatform platform, String content) {
        String[] receivers = getReceivers(platform);
        smsClient.send(platform.getSecret(), receivers, content);
    }
}
