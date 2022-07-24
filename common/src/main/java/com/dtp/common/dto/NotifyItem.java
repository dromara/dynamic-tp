package com.dtp.common.dto;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.common.util.StringUtil;
import lombok.Data;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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

    public static List<NotifyItem> mergeSimpleNotifyItems(List<NotifyItem> source) {
        // update notify items
        if (CollUtil.isEmpty(source)) {
            return getSimpleNotifyItems();
        } else {
            val excludeTypes = source.stream().map(NotifyItem::getType).collect(toList());
            val defaultItems = getSimpleNotifyItems().stream()
                    .filter(t -> !StringUtil.containsIgnoreCase(t.getType(), excludeTypes))
                    .collect(Collectors.toList());
            source.addAll(defaultItems);
            return source;
        }
    }

    public static List<NotifyItem> getSimpleNotifyItems() {
        NotifyItem changeNotify = new NotifyItem();
        changeNotify.setType(NotifyTypeEnum.CHANGE.getValue());

        NotifyItem livenessNotify = new NotifyItem();
        livenessNotify.setType(NotifyTypeEnum.LIVENESS.getValue());
        livenessNotify.setThreshold(70);

        NotifyItem capacityNotify = new NotifyItem();
        capacityNotify.setType(NotifyTypeEnum.CAPACITY.getValue());
        capacityNotify.setThreshold(70);

        List<NotifyItem> notifyItems = new ArrayList<>(6);
        notifyItems.add(livenessNotify);
        notifyItems.add(changeNotify);
        notifyItems.add(capacityNotify);

        return notifyItems;
    }

    public static List<NotifyItem> mergeAllNotifyItems(List<NotifyItem> source) {
        // update notify items
        if (CollUtil.isEmpty(source)) {
            return getAllNotifyItems();
        } else {
            val excludeTypes = source.stream().map(NotifyItem::getType).collect(toList());
            val filterItems = getAllNotifyItems().stream()
                    .filter(t -> !StringUtil.containsIgnoreCase(t.getType(), excludeTypes))
                    .collect(Collectors.toList());
            source.addAll(filterItems);
            return source;
        }
    }

    public static List<NotifyItem> getAllNotifyItems() {
        NotifyItem rejectNotify = new NotifyItem();
        rejectNotify.setType(NotifyTypeEnum.REJECT.getValue());
        rejectNotify.setThreshold(1);

        NotifyItem runTimeoutNotify = new NotifyItem();
        runTimeoutNotify.setType(NotifyTypeEnum.RUN_TIMEOUT.getValue());
        runTimeoutNotify.setThreshold(1);

        NotifyItem queueTimeoutNotify = new NotifyItem();
        queueTimeoutNotify.setType(NotifyTypeEnum.QUEUE_TIMEOUT.getValue());
        queueTimeoutNotify.setThreshold(1);

        List<NotifyItem> notifyItems = new ArrayList<>(6);
        notifyItems.addAll(getSimpleNotifyItems());
        notifyItems.add(rejectNotify);
        notifyItems.add(runTimeoutNotify);
        notifyItems.add(queueTimeoutNotify);

        return notifyItems;
    }
}
