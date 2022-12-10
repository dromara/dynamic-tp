package com.dtp.core.notify.invoker;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.dto.AlarmInfo;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.pattern.filter.Invoker;
import com.dtp.common.properties.DtpProperties;
import com.dtp.core.context.AlarmCtx;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.context.DtpNotifyCtxHolder;
import com.dtp.core.handler.NotifierHandler;
import com.dtp.core.notify.alarm.AlarmCounter;
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

        AlarmCtx alarmCtx = (AlarmCtx) context;
        val executorWrapper = alarmCtx.getExecutorWrapper();
        val notifyItem = alarmCtx.getNotifyItem();
        val notifyItemEnum = NotifyItemEnum.of(notifyItem.getType());

        DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executorWrapper.getThreadPoolName(), notifyItem.getType());
        context.setPlatforms(dtpProperties.getPlatforms());
        alarmCtx.setAlarmInfo(alarmInfo);

        DtpNotifyCtxHolder.set(context);
        NotifierHandler.getInstance().sendAlarm(notifyItemEnum);
        AlarmCounter.reset(executorWrapper.getThreadPoolName(), notifyItem.getType());
    }
}
