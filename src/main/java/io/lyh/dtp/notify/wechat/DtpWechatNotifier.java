package io.lyh.dtp.notify.wechat;

import io.lyh.dtp.core.DtpContextHolder;
import io.lyh.dtp.domain.DtpContextWrapper;
import io.lyh.dtp.common.em.NotifyTypeEnum;
import io.lyh.dtp.domain.DtpMainPropWrapper;
import io.lyh.dtp.domain.NotifyPlatform;
import io.lyh.dtp.common.em.NotifyPlatformEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DtpWechatNotifier related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-22 10:20
 * @since 1.0.0
 **/
@Slf4j
@Component
public class DtpWechatNotifier extends WechatAbsNotifier {

    @Override
    public String platform() {
        return NotifyPlatformEnum.WECHAT.name().toLowerCase();
    }

    @Override
    public void sendChangeMsg(DtpMainPropWrapper oldProp, List<String> diffs) {
        DtpContextWrapper contextWrapper = DtpContextHolder.get();
        NotifyPlatform platform = contextWrapper.getPlatform(NotifyPlatformEnum.WECHAT.name());
        String content = buildNoticeContent(platform, WechatNotifyConst.WECHAT_CHANGE_NOTICE_TEMPLATE, oldProp, diffs);
        if (StringUtils.isBlank(content)) {
            return;
        }
        doSend(platform, content);
    }

    @Override
    public void sendAlarmMsg(NotifyTypeEnum typeEnum) {
        DtpContextWrapper contextWrapper = DtpContextHolder.get();
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
