package org.dromara.dynamictp.core.notifier.manager;

import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.common.pattern.filter.InvokerChain;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.context.NoticeCtx;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolCreator;
import lombok.val;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.dromara.dynamictp.common.em.NotifyItemEnum.CHANGE;

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

    public static void destroy() {
        NOTICE_EXECUTOR.shutdownNow();
    }
}
