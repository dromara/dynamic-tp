package com.dtp.core.context;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.em.NotifyTypeEnum;
import lombok.Data;
import lombok.val;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * BaseNotifyCtx related
 *
 * @author: yanhom
 * @since 1.0.8
 */
@Data
public class BaseNotifyCtx {

    private ExecutorWrapper executorWrapper;

    private List<NotifyPlatform> platforms;

    private NotifyItem notifyItem;

    private NotifyTypeEnum notifyType;

    public BaseNotifyCtx() {}

    public BaseNotifyCtx(ExecutorWrapper wrapper, NotifyItem notifyItem, NotifyTypeEnum notifyType) {
        this.executorWrapper = wrapper;
        this.notifyItem = notifyItem;
        this.notifyType = notifyType;
    }

    public NotifyPlatform getPlatform(String platform) {
        if (CollUtil.isEmpty(platforms)) {
            return null;
        }
        val map = platforms.stream()
                .collect(toMap(x -> x.getPlatform().toLowerCase(), Function.identity(), (v1, v2) -> v2));
        return map.get(platform.toLowerCase());
    }
}
