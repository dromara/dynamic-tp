package com.dtp.core.notify.base;

import com.dtp.common.dto.NotifyPlatform;

/**
 * Notifier related
 *
 * @author yanhom
 * @date 2022-07-24 1:38 PM
 */
public interface Notifier {

    /**
     * Get the platform name.
     *
     * @return platform
     */
    String platform();

    /**
     * Send message.
     *
     * @param platform platform
     * @param content content
     */
    void send(NotifyPlatform platform, String content);

}
