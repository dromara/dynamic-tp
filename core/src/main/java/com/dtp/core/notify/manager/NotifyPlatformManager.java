package com.dtp.core.notify.manager;

import com.dtp.common.entity.NotifyPlatform;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知平台配置管理器
 *
 * @author KamTo Hung
 */
public class NotifyPlatformManager {

    private NotifyPlatformManager() {
    }

    /**
     * key: threadPoolName:platform
     */
    private static final Map<String, NotifyPlatform> NOTIFY_PLATFORM = new ConcurrentHashMap<>();

    public static void init(String threadPoolName, List<NotifyPlatform> notifyPlatforms) {
        notifyPlatforms.forEach(notifyPlatform -> {
            String key = buildKey(threadPoolName, notifyPlatform.getPlatform());
            NOTIFY_PLATFORM.putIfAbsent(key, notifyPlatform);
        });
    }

    public static NotifyPlatform getNotifyPlatform(String threadPoolName, String notifyPlatform) {
        String key = buildKey(threadPoolName, notifyPlatform);
        return NOTIFY_PLATFORM.get(key);
    }

    private static String buildKey(String threadPoolName, String platform) {
        return threadPoolName + ":" + platform;
    }

}
