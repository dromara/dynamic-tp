package com.dtp.core.notify.base;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.dtp.common.constant.LarkNotifyConst;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.em.NotifyPlatformEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * LarkNotifier
 *
 * @author fxbin
 * @version v1.0
 * @since 2022/4/28 23:25
 */
@Slf4j
public class LarkNotifier implements Notifier {

    @Override
    public String platform() {
        return NotifyPlatformEnum.LARK.name().toLowerCase();
    }

    /**
     * Execute real Lark send.
     *
     * @param notifyPlatform {@link NotifyPlatform}
     * @param text send content
     */
    @Override
    public void send(NotifyPlatform notifyPlatform, String text) {
        String serverUrl = LarkNotifyConst.LARK_WEBHOOK + notifyPlatform.getUrlKey();

        HttpResponse response = null;
        try {
            response = HttpRequest.post(serverUrl).body(text).execute();
        } catch (Exception e) {
            log.error("DynamicTp notify, lark send fail...", e);
        } finally {
            if (Objects.nonNull(response)) {
                log.info("DynamicTp notify, lark send success, response: {}, request:{}", response.body(), text);
            }
        }
    }

}
