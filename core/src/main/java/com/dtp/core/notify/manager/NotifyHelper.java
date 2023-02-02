package com.dtp.core.notify.manager;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.properties.DtpProperties;
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dtp.common.em.NotifyItemEnum.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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

    public static NotifyItem getNotifyItem(DtpExecutor executor, NotifyItemEnum notifyItemEnum) {
        val executorWrapper = new ExecutorWrapper(executor.getThreadPoolName(), executor,
                executor.getNotifyItems(), executor.isNotifyEnabled());
        return getNotifyItem(executorWrapper, notifyItemEnum);
    }

    public static NotifyItem getNotifyItem(ExecutorWrapper executorWrapper, NotifyItemEnum notifyItemEnum) {
        List<NotifyItem> notifyItems = executorWrapper.getNotifyItems();
        val notifyItemOpt = notifyItems.stream()
                .filter(x -> notifyItemEnum.getValue().equalsIgnoreCase(x.getType()))
                .findFirst();
        if (!notifyItemOpt.isPresent()) {
            log.debug("DynamicTp notify, no such [{}] notify item configured, threadPoolName: {}",
                    notifyItemEnum.getValue(), executorWrapper.getThreadPoolName());
            return null;
        }

        return notifyItemOpt.get();
    }

    public static void fillPlatforms(List<NotifyPlatform> platforms, List<NotifyItem> notifyItems) {
        if (CollectionUtils.isEmpty(platforms) || CollectionUtils.isEmpty(notifyItems)) {
            log.warn("DynamicTp notify, no notify platforms or items configured.");
            return;
        }

        List<String> platformNames = platforms.stream().map(NotifyPlatform::getPlatform).collect(toList());
        notifyItems.forEach(n -> {
            if (CollectionUtils.isEmpty(n.getPlatforms())) {
                n.setPlatforms(platformNames);
            }
        });
    }

    public static NotifyPlatform getPlatform(String platform) {
        DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        if (CollectionUtils.isEmpty(dtpProperties.getPlatforms())) {
            return null;
        }
        val map = dtpProperties.getPlatforms().stream()
                .collect(toMap(x -> x.getPlatform().toLowerCase(), Function.identity(), (v1, v2) -> v2));
        return map.get(platform.toLowerCase());
    }
}
