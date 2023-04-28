package org.dromara.dynamictp.core.handler;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.core.notifier.DtpDingNotifier;
import org.dromara.dynamictp.core.notifier.DtpLarkNotifier;
import org.dromara.dynamictp.core.notifier.DtpNotifier;
import org.dromara.dynamictp.core.notifier.DtpWechatNotifier;
import org.dromara.dynamictp.core.notifier.manager.NotifyHelper;
import org.dromara.dynamictp.core.notifier.base.DingNotifier;
import org.dromara.dynamictp.core.notifier.base.LarkNotifier;
import org.dromara.dynamictp.core.notifier.base.WechatNotifier;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;
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

    public void sendNotice(TpMainFields oldFields, List<String> diffs) {
        NotifyItem notifyItem = DtpNotifyCtxHolder.get().getNotifyItem();
        for (String platformId : notifyItem.getPlatformIds()) {
            NotifyHelper.getPlatform(platformId).ifPresent(p -> {
                DtpNotifier notifier = NOTIFIERS.get(p.getPlatform().toLowerCase());
                if (notifier != null) {
                    notifier.sendChangeMsg(p, oldFields, diffs);
                }
            });
        }
    }

    public void sendAlarm(NotifyItemEnum notifyItemEnum) {
        NotifyItem notifyItem = DtpNotifyCtxHolder.get().getNotifyItem();
        for (String platformId : notifyItem.getPlatformIds()) {
            NotifyHelper.getPlatform(platformId).ifPresent(p -> {
                DtpNotifier notifier = NOTIFIERS.get(p.getPlatform().toLowerCase());
                if (notifier != null) {
                    notifier.sendAlarmMsg(p, notifyItemEnum);
                }
            });
        }
    }

    public static NotifierHandler getInstance() {
        return NotifierHandlerHolder.INSTANCE;
    }

    private static class NotifierHandlerHolder {
        private static final NotifierHandler INSTANCE = new NotifierHandler();
    }
}
