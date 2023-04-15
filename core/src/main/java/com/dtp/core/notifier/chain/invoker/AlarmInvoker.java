package com.dtp.core.notifier.chain.invoker;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.pattern.filter.Invoker;
import com.dtp.core.notifier.context.AlarmCtx;
import com.dtp.core.notifier.context.BaseNotifyCtx;
import com.dtp.core.notifier.context.DtpNotifyCtxHolder;
import com.dtp.core.handler.NotifierHandler;
import com.dtp.core.notifier.alarm.AlarmCounter;
import lombok.val;

/**
 * AlarmInvoker related
 *
 * @author yanhom
 * @since 1.0.8
 */
public class AlarmInvoker implements Invoker<BaseNotifyCtx> {

    @Override
    public void invoke(BaseNotifyCtx context) {

        val alarmCtx = (AlarmCtx) context;
        val executorWrapper = alarmCtx.getExecutorWrapper();
        val notifyItem = alarmCtx.getNotifyItem();
        val alarmInfo = AlarmCounter.getAlarmInfo(executorWrapper.getThreadPoolName(), notifyItem.getType());
        alarmCtx.setAlarmInfo(alarmInfo);

        try {
            DtpNotifyCtxHolder.set(context);
            NotifierHandler.getInstance().sendAlarm(NotifyItemEnum.of(notifyItem.getType()));
            AlarmCounter.reset(executorWrapper.getThreadPoolName(), notifyItem.getType());
        } finally {
            DtpNotifyCtxHolder.remove();
        }
    }
}
