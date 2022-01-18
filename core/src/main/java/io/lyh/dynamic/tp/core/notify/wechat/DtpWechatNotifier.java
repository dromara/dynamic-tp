package io.lyh.dynamic.tp.core.notify.wechat;

import io.lyh.dynamic.tp.common.constant.WechatNotifyConst;
import io.lyh.dynamic.tp.common.dto.DtpMainProp;
import io.lyh.dynamic.tp.common.dto.NotifyPlatform;
import io.lyh.dynamic.tp.common.em.NotifyPlatformEnum;
import io.lyh.dynamic.tp.common.em.NotifyTypeEnum;
import io.lyh.dynamic.tp.core.context.DtpContext;
import io.lyh.dynamic.tp.core.context.DtpContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DtpWechatNotifier related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
@Component
public class DtpWechatNotifier extends AbstractWechatNotifier {

    @Override
    public String platform() {
        return NotifyPlatformEnum.WECHAT.name().toLowerCase();
    }

    @Override
    public void sendChangeMsg(DtpMainProp oldProp, List<String> diffs) {
        DtpContext contextWrapper = DtpContextHolder.get();
        NotifyPlatform platform = contextWrapper.getPlatform(NotifyPlatformEnum.WECHAT.name());
        String content = buildNoticeContent(platform, WechatNotifyConst.WECHAT_CHANGE_NOTICE_TEMPLATE, oldProp, diffs);
        if (StringUtils.isBlank(content)) {
            return;
        }
        doSend(platform, content);
    }

    @Override
    public void sendAlarmMsg(NotifyTypeEnum typeEnum) {
        DtpContext contextWrapper = DtpContextHolder.get();
        NotifyPlatform platform = contextWrapper.getPlatform(NotifyPlatformEnum.WECHAT.name());
        String content = buildAlarmContent(platform, typeEnum, WechatNotifyConst.WECHAT_ALARM_TEMPLATE);
        if (StringUtils.isBlank(content)) {
            return;
        }
        doSend(platform, content);
    }

    @Override
    protected Pair<String, String> getColors() {
        return new ImmutablePair<>(WechatNotifyConst.WARNING_COLOR, WechatNotifyConst.COMMENT_COLOR);
    }
}
