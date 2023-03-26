package com.dtp.core.notify.manager;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.entity.DtpExecutorProps;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.entity.NotifyPlatform;
import com.dtp.common.entity.TpExecutorProps;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.StreamUtil;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.thread.DtpExecutor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dtp.common.em.NotifyItemEnum.CAPACITY;
import static com.dtp.common.em.NotifyItemEnum.LIVENESS;
import static com.dtp.common.em.NotifyItemEnum.QUEUE_TIMEOUT;
import static com.dtp.common.em.NotifyItemEnum.REJECT;
import static com.dtp.common.em.NotifyItemEnum.RUN_TIMEOUT;
import static com.dtp.common.entity.NotifyItem.mergeAllNotifyItems;
import static com.dtp.common.entity.NotifyItem.mergeSimpleNotifyItems;

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

    private NotifyHelper() {
    }

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
    public static void fillPlatforms(List<String> platformIds,
                                     List<NotifyPlatform> platforms,
                                     List<NotifyItem> notifyItems) {
        if (CollectionUtils.isEmpty(platforms) || CollectionUtils.isEmpty(notifyItems)) {
            log.warn("DynamicTp notify, no notify platforms or items configured.");
            return;
        }
        List<String> globalPlatformIds = StreamUtil.fetchProperty(platforms, NotifyPlatform::getPlatformId);
        // notifyItem > executor > global
        notifyItems.forEach(n -> {
            if (CollectionUtils.isNotEmpty(n.getPlatformIds())) {
                // intersection of notifyItem and global
                n.setPlatformIds((List<String>) CollectionUtils.intersection(globalPlatformIds, n.getPlatformIds()));
            } else if (CollectionUtils.isNotEmpty(platformIds)) {
                n.setPlatformIds((List<String>) CollectionUtils.intersection(globalPlatformIds, platformIds));
            } else {
                // need to compatible with the previous situation that does not exist platformIds
                if (CollectionUtils.isNotEmpty(n.getPlatforms())) {
                    setPlatformIds(platforms, n);
                } else {
                    n.setPlatformIds(globalPlatformIds);
                }
            }
        });
    }

    private static void setPlatformIds(List<NotifyPlatform> platforms, NotifyItem notifyItem) {
        List<String> platformIds = new ArrayList<>();
        Map<String, NotifyPlatform> platformMap = StreamUtil.toMap(platforms, NotifyPlatform::getPlatform);
        for (String platform : notifyItem.getPlatforms()) {
            NotifyPlatform notifyPlatform = platformMap.get(platform);
            if (notifyPlatform != null) {
                platformIds.add(notifyPlatform.getPlatformId());
            }
        }
        notifyItem.setPlatformIds(platformIds);
    }

    public static Optional<NotifyPlatform> getPlatform(String platformId) {
        Map<String, NotifyPlatform> platformMap = getAllPlatforms();
        return Optional.ofNullable(platformMap.get(platformId));
    }

    public static Map<String, NotifyPlatform> getAllPlatforms() {
        val dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        if (Objects.isNull(dtpProperties) || CollectionUtils.isEmpty(dtpProperties.getPlatforms())) {
            return Collections.emptyMap();
        }
        return StreamUtil.toMap(dtpProperties.getPlatforms(), NotifyPlatform::getPlatformId);
    }

    public static void initNotify(DtpExecutor executor) {
        val dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        if (Objects.isNull(dtpProperties)) {
            log.warn("DynamicTp notify, cannot find a DtpProperties instance for [{}].",
                    executor.getThreadPoolName());
            return;
        }
        val platforms = dtpProperties.getPlatforms();
        if (CollectionUtils.isEmpty(platforms)) {
            executor.setNotifyItems(Lists.newArrayList());
            executor.setPlatformIds(Lists.newArrayList());
            log.warn("DynamicTp notify, no notify platforms configured for [{}]", executor.getThreadPoolName());
            return;
        }
        if (CollectionUtils.isEmpty(executor.getNotifyItems())) {
            log.warn("DynamicTp notify, no notify items configured for [{}]", executor.getThreadPoolName());
            return;
        }
        fillPlatforms(executor.getPlatformIds(), platforms, executor.getNotifyItems());
        AlarmManager.initAlarm(executor.getThreadPoolName(), executor.getNotifyItems());
    }

    public static void updateNotifyInfo(ExecutorWrapper executorWrapper,
                                        TpExecutorProps props,
                                        List<NotifyPlatform> platforms) {
        // update notify items
        val allNotifyItems = mergeSimpleNotifyItems(props.getNotifyItems());
        refreshNotify(executorWrapper.getThreadPoolName(),
                props.getPlatformIds(),
                platforms,
                executorWrapper.getNotifyItems(),
                allNotifyItems);
        executorWrapper.setNotifyItems(allNotifyItems);
        executorWrapper.setPlatformIds(props.getPlatformIds());
        executorWrapper.setNotifyEnabled(props.isNotifyEnabled());
    }

    public static void updateNotifyInfo(DtpExecutor executor, DtpExecutorProps props, List<NotifyPlatform> platforms) {
        // update notify items
        val allNotifyItems = mergeAllNotifyItems(props.getNotifyItems());
        refreshNotify(executor.getThreadPoolName(),
                props.getPlatformIds(),
                platforms,
                executor.getNotifyItems(),
                allNotifyItems);
        executor.setNotifyItems(allNotifyItems);
        executor.setPlatformIds(props.getPlatformIds());
        executor.setNotifyEnabled(props.isNotifyEnabled());
    }

    private static void refreshNotify(String poolName,
                                      List<String> platformIds,
                                      List<NotifyPlatform> platforms,
                                      List<NotifyItem> oldNotifyItems,
                                      List<NotifyItem> newNotifyItems) {
        fillPlatforms(platformIds, platforms, newNotifyItems);
        Map<String, NotifyItem> oldNotifyItemMap = StreamUtil.toMap(oldNotifyItems, NotifyItem::getType);
        newNotifyItems.forEach(x -> {
            NotifyItem oldNotifyItem = oldNotifyItemMap.get(x.getType());
            if (Objects.nonNull(oldNotifyItem) && oldNotifyItem.getInterval() == x.getInterval()) {
                return;
            }
            AlarmManager.initAlarm(poolName, x);
        });
    }
}
