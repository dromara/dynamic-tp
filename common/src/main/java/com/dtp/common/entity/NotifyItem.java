package com.dtp.common.entity;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.common.util.StringUtil;
import lombok.Data;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * NotifyItem related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
public class NotifyItem {

    /**
     * @deprecated, use {@link #platformIds} instead, remove in 1.1.2
     * Notify platform names, see {@link NotifyPlatformEnum}
     */
    @Deprecated
    private List<String> platforms;

    /**
     * Notify platform id
     */
    private List<String> platformIds;

    /**
     * If enabled notify.
     */
    private boolean enabled = true;

    /**
     * Notify item, see {@link NotifyItemEnum}
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
     * Cluster notify limit.
     */
    private int clusterLimit = 1;

    public static List<NotifyItem> mergeSimpleNotifyItems(List<NotifyItem> source) {
        // update notify items
        if (CollectionUtils.isEmpty(source)) {
            return getSimpleNotifyItems();
        } else {
            val configuredTypes = source.stream().map(NotifyItem::getType).collect(toList());
            val defaultItems = getSimpleNotifyItems().stream()
                    .filter(t -> !StringUtil.containsIgnoreCase(t.getType(), configuredTypes))
                    .collect(Collectors.toList());
            source.addAll(defaultItems);
            return source;
        }
    }

    public static List<NotifyItem> getSimpleNotifyItems() {
        NotifyItem changeNotify = new NotifyItem();
        changeNotify.setType(NotifyItemEnum.CHANGE.getValue());
        changeNotify.setInterval(1);

        NotifyItem livenessNotify = new NotifyItem();
        livenessNotify.setType(NotifyItemEnum.LIVENESS.getValue());
        livenessNotify.setThreshold(70);

        NotifyItem capacityNotify = new NotifyItem();
        capacityNotify.setType(NotifyItemEnum.CAPACITY.getValue());
        capacityNotify.setThreshold(70);

        List<NotifyItem> notifyItems = new ArrayList<>(3);
        notifyItems.add(livenessNotify);
        notifyItems.add(changeNotify);
        notifyItems.add(capacityNotify);

        return notifyItems;
    }

    public static List<NotifyItem> mergeAllNotifyItems(List<NotifyItem> source) {
        // update notify items
        if (CollectionUtils.isEmpty(source)) {
            return getAllNotifyItems();
        } else {
            val configuredTypes = source.stream().map(NotifyItem::getType).collect(toList());
            val defaultItems = getAllNotifyItems().stream()
                    .filter(t -> !StringUtil.containsIgnoreCase(t.getType(), configuredTypes))
                    .collect(Collectors.toList());
            source.addAll(defaultItems);
            return source;
        }
    }

    public static List<NotifyItem> getAllNotifyItems() {
        NotifyItem rejectNotify = new NotifyItem();
        rejectNotify.setType(NotifyItemEnum.REJECT.getValue());
        rejectNotify.setThreshold(1);

        NotifyItem runTimeoutNotify = new NotifyItem();
        runTimeoutNotify.setType(NotifyItemEnum.RUN_TIMEOUT.getValue());
        runTimeoutNotify.setThreshold(1);

        NotifyItem queueTimeoutNotify = new NotifyItem();
        queueTimeoutNotify.setType(NotifyItemEnum.QUEUE_TIMEOUT.getValue());
        queueTimeoutNotify.setThreshold(1);

        List<NotifyItem> notifyItems = new ArrayList<>(6);
        notifyItems.addAll(getSimpleNotifyItems());
        notifyItems.add(rejectNotify);
        notifyItems.add(runTimeoutNotify);
        notifyItems.add(queueTimeoutNotify);

        return notifyItems;
    }
}
