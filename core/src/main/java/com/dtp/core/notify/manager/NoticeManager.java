package com.dtp.core.notify.manager;

import com.dtp.common.entity.TpMainFields;
import com.dtp.common.pattern.filter.InvokerChain;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.context.NoticeCtx;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.support.ThreadPoolCreator;
import lombok.val;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.dtp.common.em.NotifyItemEnum.CHANGE;

/**
 * NoticeManager related
 *
 * @author yanhom
 * @since 1.0.8
 */
public class NoticeManager {

    private static final ExecutorService NOTICE_EXECUTOR = ThreadPoolCreator.createCommonFast("dtp-notify");

    private NoticeManager() { }

    private static final InvokerChain<BaseNotifyCtx> NOTICE_INVOKER_CHAIN;

    static {
        NOTICE_INVOKER_CHAIN = NotifyFilterBuilder.getCommonInvokerChain();
        Runtime.getRuntime().addShutdownHook(new Thread(NOTICE_EXECUTOR::shutdown));
    }

    public static void doNoticeAsync(ExecutorWrapper executor, TpMainFields oldFields, List<String> diffKeys) {
        NOTICE_EXECUTOR.execute(() -> doNotice(executor, oldFields, diffKeys));
    }

    public static void doNotice(ExecutorWrapper executor, TpMainFields oldFields, List<String> diffKeys) {
        NotifyHelper.getNotifyItem(executor, CHANGE).ifPresent(notifyItem -> {
            val noticeCtx = new NoticeCtx(executor, notifyItem, oldFields, diffKeys);
            NOTICE_INVOKER_CHAIN.proceed(noticeCtx);
        });
    }
}
