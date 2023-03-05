package com.dtp.core.notify.manager;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.entity.NotifyPlatform;
import com.dtp.common.util.StreamUtil;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.thread.DtpExecutor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dtp.common.em.NotifyItemEnum.*;
import static java.util.stream.Collectors.toList;

/**
 * NotifyHelper related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class NotifyHelper {

    private static final List<String> COMMON_ALARM_KEYS = Lists.newArrayList("alarmType", "threshold");

    private static final Set<String> LIVENESS_ALARM_KEYS = Sets.newHashSet(
            "corePoolSize", "maximumPoolSize", "poolSize", "activeCount");

    private static final Set<String> CAPACITY_ALARM_KEYS = Sets.newHashSet(
            "queueType", "queueCapacity", "queueSize", "queueRemaining");

    private static final Set<String> REJECT_ALARM_KEYS = Sets.newHashSet("rejectType", "rejectCount");

    private static final Set<String> RUN_TIMEOUT_ALARM_KEYS = Sets.newHashSet("runTimeoutCount");

    private static final Set<String> QUEUE_TIMEOUT_ALARM_KEYS = Sets.newHashSet("queueTimeoutCount");

    private static final Set<String> ALL_ALARM_KEYS;

    private static final Map<String, Set<String>> ALARM_KEYS = Maps.newHashMap();

    static {
        ALARM_KEYS.put(LIVENESS.name(), LIVENESS_ALARM_KEYS);
        ALARM_KEYS.put(CAPACITY.name(), CAPACITY_ALARM_KEYS);
        ALARM_KEYS.put(REJECT.name(), REJECT_ALARM_KEYS);
        ALARM_KEYS.put(RUN_TIMEOUT.name(), RUN_TIMEOUT_ALARM_KEYS);
        ALARM_KEYS.put(QUEUE_TIMEOUT.name(), QUEUE_TIMEOUT_ALARM_KEYS);

        ALL_ALARM_KEYS = ALARM_KEYS.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        ALL_ALARM_KEYS.addAll(COMMON_ALARM_KEYS);
    }

    private NotifyHelper() { }

    public static Set<String> getAllAlarmKeys() {
        return ALL_ALARM_KEYS;
    }

    public static Set<String> getAlarmKeys(NotifyItemEnum notifyItemEnum) {
        val keys = ALARM_KEYS.get(notifyItemEnum.name());
        keys.addAll(COMMON_ALARM_KEYS);
        return keys;
    }

    public static Optional<NotifyItem> getNotifyItem(ExecutorWrapper executor, NotifyItemEnum notifyType) {
        return executor.getNotifyItems().stream()
                .filter(x -> notifyType.getValue().equalsIgnoreCase(x.getType()))
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    public static void fillPlatforms(List<NotifyPlatform> platforms, List<NotifyItem> notifyItems) {
        if (CollectionUtils.isEmpty(platforms) || CollectionUtils.isEmpty(notifyItems)) {
            log.warn("DynamicTp notify, no notify platforms or items configured.");
            return;
        }

        List<String> platformNames = platforms.stream().map(NotifyPlatform::getPlatform).collect(toList());
        notifyItems.forEach(n -> {
            if (CollectionUtils.isEmpty(n.getPlatforms())) {
                n.setPlatforms(platformNames);
            } else {
                n.setPlatforms((List<String>) CollectionUtils.intersection(platformNames, n.getPlatforms()));
            }
        });
    }

    public static Optional<NotifyPlatform> getPlatform(String threadPoolName, String platform) {
        return Optional.ofNullable(NotifyPlatformManager.getNotifyPlatform(threadPoolName, platform));
    }

    public static void initNotify(DtpExecutor executor, List<NotifyPlatform> platforms) {
        if (CollectionUtils.isEmpty(platforms)) {
            executor.setNotifyItems(Lists.newArrayList());
            log.warn("DynamicTp notify, no notify platforms configured, name {}", executor.getThreadPoolName());
            return;
        }
        if (CollectionUtils.isEmpty(executor.getNotifyItems())) {
            log.warn("DynamicTp notify, no notify items configured, name {}", executor.getThreadPoolName());
            return;
        }
        fillPlatforms(platforms, executor.getNotifyItems());
        AlarmManager.initAlarm(executor.getThreadPoolName(), executor.getNotifyItems());
        // init notify platform
        NotifyPlatformManager.init(executor.getThreadPoolName(), platforms);
    }

    public static void refreshNotify(String poolName,
                                     List<NotifyPlatform> platforms,
                                     List<NotifyItem> oldItems,
                                     List<NotifyItem> newItems) {
        fillPlatforms(platforms, newItems);
        Map<String, NotifyItem> oldNotifyItemMap = StreamUtil.toMap(oldItems, NotifyItem::getType);
        newItems.forEach(x -> {
            NotifyItem oldNotifyItem = oldNotifyItemMap.get(x.getType());
            if (Objects.nonNull(oldNotifyItem) && oldNotifyItem.getInterval() == x.getInterval()) {
                return;
            }
            AlarmManager.initAlarm(poolName, x);
        });
    }
}
