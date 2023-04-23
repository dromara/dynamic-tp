package org.dromara.dynamictp.core.notifier.chain.invoker;

import org.dromara.dynamictp.common.pattern.filter.Invoker;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;
import org.dromara.dynamictp.core.notifier.context.NoticeCtx;
import org.dromara.dynamictp.core.handler.NotifierHandler;
import lombok.val;

/**
 * NoticeInvoker related
 *
 * @author yanhom
 * @since 1.0.8
 */
public class NoticeInvoker implements Invoker<BaseNotifyCtx> {

    @Override
    public void invoke(BaseNotifyCtx context) {
        try {
            DtpNotifyCtxHolder.set(context);
            val noticeCtx = (NoticeCtx) context;
            NotifierHandler.getInstance().sendNotice(noticeCtx.getOldFields(), noticeCtx.getDiffs());
        } finally {
            DtpNotifyCtxHolder.remove();
        }
    }
}
