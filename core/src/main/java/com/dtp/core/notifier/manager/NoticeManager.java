package com.dtp.core.notifier.manager;

import com.dtp.common.entity.TpMainFields;
import com.dtp.common.pattern.filter.InvokerChain;
import com.dtp.core.notifier.context.BaseNotifyCtx;
import com.dtp.core.notifier.context.NoticeCtx;
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

    private static ExecutorService noticeExecutor;

    private NoticeManager() { }

    private static InvokerChain<BaseNotifyCtx> noticeInvokerChain;

    public static void doNoticeAsync(ExecutorWrapper executor, TpMainFields oldFields, List<String> diffKeys) {
        noticeExecutor.execute(() -> doNotice(executor, oldFields, diffKeys));
    }

    public static void doNotice(ExecutorWrapper executor, TpMainFields oldFields, List<String> diffKeys) {
        NotifyHelper.getNotifyItem(executor, CHANGE).ifPresent(notifyItem -> {
            val noticeCtx = new NoticeCtx(executor, notifyItem, oldFields, diffKeys);
            noticeInvokerChain.proceed(noticeCtx);
        });
    }

    public static void initialize() {
        noticeInvokerChain = NotifyFilterBuilder.getCommonInvokerChain();
        noticeExecutor = ThreadPoolCreator.createCommonFast("dtp-notify");
    }

    public static void destroy() {
        noticeExecutor.shutdownNow();
    }

}
