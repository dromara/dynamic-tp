package com.dtp.core.notify.wechat;

import com.dtp.common.constant.WechatNotifyConst;
import com.dtp.common.dto.DtpMainProp;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.context.DtpContext;
import com.dtp.core.context.DtpContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dtp.common.constant.WechatNotifyConst.WECHAT_ALARM_TEMPLATE;
import static com.dtp.common.constant.WechatNotifyConst.WECHAT_CHANGE_NOTICE_TEMPLATE;

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
        String content = buildNoticeContent(platform, WECHAT_CHANGE_NOTICE_TEMPLATE, oldProp, diffs);
        if (StringUtils.isBlank(content)) {
            return;
        }
        doSend(platform, content);
    }

    @Override
    public void sendAlarmMsg(NotifyTypeEnum typeEnum) {
        DtpContext contextWrapper = DtpContextHolder.get();
        NotifyPlatform platform = contextWrapper.getPlatform(NotifyPlatformEnum.WECHAT.name());
        String content = buildAlarmContent(platform, typeEnum, WECHAT_ALARM_TEMPLATE);
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
