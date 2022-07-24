package com.dtp.core.handler;

import com.dtp.common.dto.DtpMainProp;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.context.DtpContextHolder;
import com.dtp.core.notify.DtpNotifier;
import com.dtp.core.notify.base.DingNotifier;
import com.dtp.core.notify.base.LarkNotifier;
import com.dtp.core.notify.base.WechatNotifier;
import com.dtp.core.notify.DtpDingNotifier;
import com.dtp.core.notify.DtpLarkNotifier;
import com.dtp.core.notify.DtpWechatNotifier;
import com.dtp.core.support.ThreadPoolCreator;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;

/**
 * NotifierHandler related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class NotifierHandler {

    private static final ExecutorService NOTICE_EXECUTOR = ThreadPoolCreator.createCommonWithTtl("dtp-notify");

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

    public void sendNoticeAsync(DtpMainProp prop, List<String> diffs) {
        NOTICE_EXECUTOR.execute(() -> sendNotice(prop, diffs));
    }

    public void sendNotice(DtpMainProp prop, List<String> diffs) {

        try {
            NotifyItem notifyItem = DtpContextHolder.get().getNotifyItem();
            for (String platform : notifyItem.getPlatforms()) {
                DtpNotifier notifier = NOTIFIERS.get(platform.toLowerCase());
                if (notifier != null) {
                    notifier.sendChangeMsg(prop, diffs);
                }
            }
        } finally {
            DtpContextHolder.remove();
        }
    }

    public void sendAlarm(NotifyTypeEnum typeEnum) {
        try {
            NotifyItem notifyItem = DtpContextHolder.get().getNotifyItem();
            for (String platform : notifyItem.getPlatforms()) {
                DtpNotifier notifier = NOTIFIERS.get(platform.toLowerCase());
                if (notifier != null) {
                    notifier.sendAlarmMsg(typeEnum);
                }
            }
        } finally {
            DtpContextHolder.remove();
        }
    }

    public static NotifierHandler getInstance() {
        return NotifierHandlerHolder.INSTANCE;
    }

    private static class NotifierHandlerHolder {
        private static final NotifierHandler INSTANCE = new NotifierHandler();
    }
}
