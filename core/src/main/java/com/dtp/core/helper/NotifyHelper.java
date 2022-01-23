package com.dtp.core.helper;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.ThreadPoolProperties;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.common.util.StreamUtil;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.notify.AlarmLimiter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * NotifyHelper related
 *
 * @author: yanhom
 * @since 1.0.0
 */
@Slf4j
public class NotifyHelper {

    private NotifyHelper() {}

    public static NotifyItem getNotifyItem(DtpExecutor dtpExecutor, NotifyTypeEnum typeEnum) {
        List<NotifyItem> notifyItems = dtpExecutor.getNotifyItems();
        NotifyItem notifyItem = notifyItems.stream()
                .filter(x -> typeEnum.getValue().equalsIgnoreCase(x.getType()) && x.isEnabled())
                .findFirst()
                .orElse(null);
        if (Objects.isNull(notifyItem)) {
            log.warn("DynamicTp notify, no such [{}] notify item configured, threadPoolName: {}",
                    typeEnum.getValue(), dtpExecutor.getThreadPoolName());
            return null;
        }

        return notifyItem;
    }

    public static void fillNotifyItems(List<NotifyPlatform> platforms, List<NotifyItem> notifyItems) {
        if (CollUtil.isEmpty(platforms)) {
            log.warn("DynamicTp notify, no notify platforms configured...");
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
        });
    }
}
