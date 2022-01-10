package io.lyh.dtp.core;

import cn.hutool.core.collection.CollUtil;
import io.lyh.dtp.notify.NotifyItem;
import io.lyh.dtp.notify.NotifyPlatform;
import lombok.Builder;
import lombok.Data;
import lombok.val;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * DtpContentWrapper related
 *
 * @author: yanhom
 * @since 1.0.0
 */
@Builder
@Data
public class DtpContext {

    private DtpExecutor dtpExecutor;

    private List<NotifyPlatform> platforms;

    private NotifyItem notifyItem;

    public NotifyPlatform getPlatform(String platform) {
        if (CollUtil.isEmpty(platforms)) {
            return null;
        }
        val map = platforms.stream()
                .collect(toMap(x -> x.getPlatform().toLowerCase(), Function.identity(), (v1, v2) -> v2));
        return map.get(platform.toLowerCase());
    }
}
