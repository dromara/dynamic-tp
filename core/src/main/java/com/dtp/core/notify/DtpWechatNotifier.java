package com.dtp.core.notify;

import com.dtp.common.constant.WechatNotifyConst;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.core.notify.base.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import static com.dtp.common.constant.WechatNotifyConst.WECHAT_ALARM_TEMPLATE;
import static com.dtp.common.constant.WechatNotifyConst.WECHAT_CHANGE_NOTICE_TEMPLATE;

/**
 * DtpWechatNotifier related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpWechatNotifier extends AbstractDtpNotifier {

    public DtpWechatNotifier(Notifier notifier) {
        super(notifier);
    }

    @Override
    public String platform() {
        return NotifyPlatformEnum.WECHAT.name().toLowerCase();
    }

    @Override
    protected String getNoticeTemplate() {
        return WECHAT_CHANGE_NOTICE_TEMPLATE;
    }

    @Override
    protected String getAlarmTemplate() {
        return WECHAT_ALARM_TEMPLATE;
    }

    @Override
    protected Pair<String, String> getColors() {
        return new ImmutablePair<>(WechatNotifyConst.WARNING_COLOR, WechatNotifyConst.COMMENT_COLOR);
    }
}
