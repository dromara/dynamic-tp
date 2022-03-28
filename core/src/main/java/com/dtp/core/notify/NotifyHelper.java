package com.dtp.core.notify;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.ThreadPoolProperties;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.common.util.StreamUtil;
import com.dtp.core.thread.DtpExecutor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

import static com.dtp.common.em.NotifyTypeEnum.*;
import static java.util.stream.Collectors.toList;

/**
 * NotifyHelper related
 *
 * @author: yanhom
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

    private NotifyHelper() {}

    public static Set<String> getAllAlarmKeys() {
        return ALL_ALARM_KEYS;
    }

    public static Set<String> getAlarmKeys(NotifyTypeEnum typeEnum) {
        val keys = ALARM_KEYS.get(typeEnum.name());
        keys.addAll(COMMON_ALARM_KEYS);
        return keys;
    }

    public static NotifyItem getNotifyItem(DtpExecutor dtpExecutor, NotifyTypeEnum typeEnum) {
        List<NotifyItem> notifyItems = dtpExecutor.getNotifyItems();
        NotifyItem notifyItem = notifyItems.stream()
                .filter(x -> typeEnum.getValue().equalsIgnoreCase(x.getType()) && x.isEnabled())
                .findFirst()
                .orElse(null);
        if (Objects.isNull(notifyItem)) {
            log.debug("DynamicTp notify, no such [{}] notify item configured, threadPoolName: {}",
                    typeEnum.getValue(), dtpExecutor.getThreadPoolName());
            return null;
        }

        return notifyItem;
    }

    public static void fillNotifyItems(List<NotifyPlatform> platforms, List<NotifyItem> notifyItems) {
        if (CollUtil.isEmpty(platforms)) {
            log.warn("DynamicTp notify, no notify platforms configured.");
            return;
        }

        List<String> platformNames = platforms.stream()
                .map(NotifyPlatform::getPlatform).collect(toList());
        notifyItems.forEach(n -> {
            if (CollUtil.isEmpty(n.getPlatforms())) {
                n.setPlatforms(platformNames);
            }
        });
    }

    public static void setExecutorNotifyItems(DtpExecutor dtpExecutor,
                                              DtpProperties dtpProperties,
                                              ThreadPoolProperties properties) {
        fillNotifyItems(dtpProperties.getPlatforms(), properties.getNotifyItems());
        List<NotifyItem> oldNotifyItems = dtpExecutor.getNotifyItems();
        Map<String, NotifyItem> oldNotifyItemMap = StreamUtil.toMap(oldNotifyItems, NotifyItem::getType);
        properties.getNotifyItems().forEach(x -> {
            NotifyItem oldNotifyItem = oldNotifyItemMap.get(x.getType());
            if (Objects.nonNull(oldNotifyItem) && oldNotifyItem.getInterval() == x.getInterval()) {
                return;
            }
            AlarmLimiter.initAlarmLimiter(dtpExecutor.getThreadPoolName(), x);
            AlarmCounter.init(dtpExecutor.getThreadPoolName(), x.getType());
        });
    }
}
