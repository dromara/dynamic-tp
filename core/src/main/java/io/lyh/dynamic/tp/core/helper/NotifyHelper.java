package io.lyh.dynamic.tp.core.helper;

import cn.hutool.core.collection.CollUtil;
import io.lyh.dynamic.tp.common.dto.NotifyItem;
import io.lyh.dynamic.tp.common.dto.NotifyPlatform;
import io.lyh.dynamic.tp.common.em.NotifyTypeEnum;
import io.lyh.dynamic.tp.core.DtpExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
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

        List<String> platformNames = platforms.stream().map(NotifyPlatform::getPlatform).collect(toList());
        notifyItems.forEach(n -> {
            if (CollUtil.isEmpty(n.getPlatforms())) {
                n.setPlatforms(platformNames);
            }
        });
    }
}
