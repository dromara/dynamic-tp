package com.dtp.core.handler;

import com.dtp.common.entity.DtpMainProp;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.core.context.DtpNotifyCtxHolder;
import com.dtp.core.notify.DtpDingNotifier;
import com.dtp.core.notify.DtpLarkNotifier;
import com.dtp.core.notify.DtpNotifier;
import com.dtp.core.notify.DtpWechatNotifier;
import com.dtp.core.notify.base.DingNotifier;
import com.dtp.core.notify.base.LarkNotifier;
import com.dtp.core.notify.base.WechatNotifier;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * NotifierHandler related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public final class NotifierHandler {

    private static final Map<String, DtpNotifier> NOTIFIERS = new HashMap<>();

    private NotifierHandler() {
        ServiceLoader<DtpNotifier> loader = ServiceLoader.load(DtpNotifier.class);
        for (DtpNotifier notifier : loader) {
            NOTIFIERS.put(notifier.platform(), notifier);
        }

        DtpNotifier dingNotifier = new DtpDingNotifier(new DingNotifier());
        DtpNotifier wechatNotifier = new DtpWechatNotifier(new WechatNotifier());
        DtpNotifier larkNotifier = new DtpLarkNotifier(new LarkNotifier());
        NOTIFIERS.put(dingNotifier.platform(), dingNotifier);
        NOTIFIERS.put(wechatNotifier.platform(), wechatNotifier);
        NOTIFIERS.put(larkNotifier.platform(), larkNotifier);
    }

    public void sendNotice(String threadPoolName, DtpMainProp prop, List<String> diffs) {
        NotifyItem notifyItem = DtpNotifyCtxHolder.get().getNotifyItem();
        for (String platform : notifyItem.getPlatforms()) {
            DtpNotifier notifier = NOTIFIERS.get(platform.toLowerCase());
            if (notifier != null) {
                notifier.sendChangeMsg(threadPoolName, prop, diffs);
            }
        }
    }

    public void sendAlarm(String threadPoolName, NotifyItemEnum notifyItemEnum) {
        NotifyItem notifyItem = DtpNotifyCtxHolder.get().getNotifyItem();
        for (String platform : notifyItem.getPlatforms()) {
            DtpNotifier notifier = NOTIFIERS.get(platform.toLowerCase());
            if (notifier != null) {
                notifier.sendAlarmMsg(threadPoolName, notifyItemEnum);
            }
        }
    }

    public static NotifierHandler getInstance() {
        return NotifierHandlerHolder.INSTANCE;
    }

    private static class NotifierHandlerHolder {
        private static final NotifierHandler INSTANCE = new NotifierHandler();
    }
}
