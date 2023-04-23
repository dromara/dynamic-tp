package org.dromara.dynamictp.core.notifier.chain.invoker;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.pattern.filter.Invoker;
import org.dromara.dynamictp.core.notifier.context.AlarmCtx;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;
import org.dromara.dynamictp.core.handler.NotifierHandler;
import org.dromara.dynamictp.core.notifier.alarm.AlarmCounter;
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
