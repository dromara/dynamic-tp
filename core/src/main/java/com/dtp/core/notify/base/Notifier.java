package com.dtp.core.notify.base;

import com.dtp.common.entity.NotifyPlatform;

/**
 * Notifier related
 *
 * @author yanhom
 * @since 1.0.8
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
