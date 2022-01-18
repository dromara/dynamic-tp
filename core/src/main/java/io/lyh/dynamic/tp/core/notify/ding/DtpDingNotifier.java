package io.lyh.dynamic.tp.core.notify.ding;

import cn.hutool.core.util.StrUtil;
import io.lyh.dynamic.tp.common.constant.DingNotifyConst;
import io.lyh.dynamic.tp.common.dto.DtpMainProp;
import io.lyh.dynamic.tp.common.constant.DynamicTpConst;
import io.lyh.dynamic.tp.common.em.NotifyPlatformEnum;
import io.lyh.dynamic.tp.common.em.NotifyTypeEnum;
import io.lyh.dynamic.tp.common.dto.NotifyPlatform;
import io.lyh.dynamic.tp.core.context.DtpContext;
import io.lyh.dynamic.tp.core.context.DtpContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static io.lyh.dynamic.tp.common.constant.DingNotifyConst.CONTENT_COLOR;
import static io.lyh.dynamic.tp.common.constant.DingNotifyConst.WARNING_COLOR;

/**
 * DtpDingNotifier related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpDingNotifier extends AbstractDingNotifier {

    @Override
    public String platform() {
        return NotifyPlatformEnum.DING.name().toLowerCase();
    }

    @Override
    public void sendChangeMsg(DtpMainProp oldProp, List<String> diffs) {
        DtpContext contextWrapper = DtpContextHolder.get();
        NotifyPlatform platform = contextWrapper.getPlatform(NotifyPlatformEnum.DING.name());
        String content = buildNoticeContent(platform, DingNotifyConst.DING_CHANGE_NOTICE_TEMPLATE, oldProp, diffs);
        if (StringUtils.isBlank(content)) {
            return;
        }

        List<String> receivesList = StrUtil.split(platform.getReceivers(), ',');
        doSend(platform, DingNotifyConst.DING_NOTICE_TITLE, content, receivesList);
    }

    @Override
    public void sendAlarmMsg(NotifyTypeEnum typeEnum) {
        DtpContext contextWrapper = DtpContextHolder.get();
        NotifyPlatform platform = contextWrapper.getPlatform(NotifyPlatformEnum.DING.name());
        String content = buildAlarmContent(platform, typeEnum, DingNotifyConst.DING_ALARM_TEMPLATE);
        if (StringUtils.isBlank(content)) {
            return;
        }

        List<String> receivesList = StrUtil.split(platform.getReceivers(), ',');
        doSend(platform, DingNotifyConst.DING_ALARM_TITLE, content, receivesList);
    }

    @Override
    protected Pair<String, String> getColors() {
        return new ImmutablePair<>(DingNotifyConst.WARNING_COLOR, DingNotifyConst.CONTENT_COLOR);
    }
}
