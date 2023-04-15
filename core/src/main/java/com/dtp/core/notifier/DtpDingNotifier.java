package com.dtp.core.notifier;

import com.dtp.common.constant.DingNotifyConst;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.core.notifier.base.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * DtpDingNotifier related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpDingNotifier extends AbstractDtpNotifier {

    public DtpDingNotifier(Notifier notifier) {
        super(notifier);
    }

    @Override
    public String platform() {
        return NotifyPlatformEnum.DING.name().toLowerCase();
    }

    @Override
    protected String getNoticeTemplate() {
        return DingNotifyConst.DING_CHANGE_NOTICE_TEMPLATE;
    }

    @Override
    protected String getAlarmTemplate() {
        return DingNotifyConst.DING_ALARM_TEMPLATE;
    }

    @Override
    protected Pair<String, String> getColors() {
        return new ImmutablePair<>(DingNotifyConst.WARNING_COLOR, DingNotifyConst.CONTENT_COLOR);
    }
}
