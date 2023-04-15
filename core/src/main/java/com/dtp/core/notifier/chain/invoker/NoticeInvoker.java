package com.dtp.core.notifier.chain.invoker;

import com.dtp.common.pattern.filter.Invoker;
import com.dtp.core.notifier.context.BaseNotifyCtx;
import com.dtp.core.notifier.context.DtpNotifyCtxHolder;
import com.dtp.core.notifier.context.NoticeCtx;
import com.dtp.core.handler.NotifierHandler;
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
