package com.dtp.core.notify.manager;

import com.dtp.common.pattern.filter.InvokerChain;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.context.NoticeCtx;
import com.dtp.core.support.ThreadPoolCreator;

import java.util.concurrent.ExecutorService;

/**
 * NoticeManager related
 *
 * @author yanhom
 * @since 1.0.8
 */
public class NoticeManager {

    private static final ExecutorService NOTICE_EXECUTOR = ThreadPoolCreator.createCommonFast("dtp-notify");

    private NoticeManager() { }

    private static final InvokerChain<BaseNotifyCtx> NOTICE_FILTER_CHAIN;

    static {
        NOTICE_FILTER_CHAIN = NotifyFilterBuilder.getCommonNoticeFilter();
    }

    public static void doNotice(NoticeCtx noticeCtx) {
        NOTICE_FILTER_CHAIN.proceed(noticeCtx);
    }

    public static void doNoticeAsync(NoticeCtx noticeCtx) {
        NOTICE_EXECUTOR.execute(() -> doNotice(noticeCtx));
    }
}
