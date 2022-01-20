package com.dtp.common.dto;

import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.common.em.NotifyTypeEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * NotifyItem related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Data
public class NotifyItem {

    /**
     * Notify platform names, see {@link NotifyPlatformEnum}
     */
    private List<String> platforms;

    /**
     * If enabled notify.
     */
    private boolean enabled = true;

    /**
     * Notify type, see {@link NotifyTypeEnum}
     */
    private String type;

    /**
     * Alarm threshold.
     */
    private int threshold;

    /**
     * Alarm interval, time unit（s）
     */
    private int interval = 120;

    /**
     * Default notify items.
     */
    private static final List<NotifyItem> DEFAULT_NOTIFY_ITEMS;

    static {
        NotifyItem changeNotify = new NotifyItem();
        changeNotify.setType(NotifyTypeEnum.CHANGE.getValue());

        NotifyItem livenessNotify = new NotifyItem();
        livenessNotify.setType(NotifyTypeEnum.LIVENESS.getValue());
        livenessNotify.setThreshold(80);

        NotifyItem capacityNotify = new NotifyItem();
        capacityNotify.setType(NotifyTypeEnum.CAPACITY.getValue());
        capacityNotify.setThreshold(80);

        NotifyItem rejectNotify = new NotifyItem();
        rejectNotify.setType(NotifyTypeEnum.REJECT.getValue());
        rejectNotify.setThreshold(1);

        DEFAULT_NOTIFY_ITEMS = new ArrayList<>(4);
        DEFAULT_NOTIFY_ITEMS.add(livenessNotify);
        DEFAULT_NOTIFY_ITEMS.add(changeNotify);
        DEFAULT_NOTIFY_ITEMS.add(capacityNotify);
        DEFAULT_NOTIFY_ITEMS.add(rejectNotify);
    }

    public static List<NotifyItem> getDefaultNotifyItems() {
        return DEFAULT_NOTIFY_ITEMS;
    }
}
