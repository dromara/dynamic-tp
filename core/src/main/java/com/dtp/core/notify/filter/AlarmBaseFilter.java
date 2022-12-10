package com.dtp.core.notify.filter;

import com.dtp.common.dto.NotifyItem;
import com.dtp.common.pattern.filter.Invoker;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.notify.alarm.AlarmLimiter;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.notify.manager.NotifyItemManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Objects;

/**
 * AlarmBaseFilter related
 *
 * @author yanhom
 * @since 1.0.8
 **/
@Slf4j
public class AlarmBaseFilter implements NotifyFilter {

    private static final Object SEND_LOCK = new Object();

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void doFilter(BaseNotifyCtx context, Invoker<BaseNotifyCtx> nextFilter) {

        val executorWrapper = context.getExecutorWrapper();
        val notifyItemEnum = context.getNotifyItemEnum();
        NotifyItem notifyItem = NotifyItemManager.getNotifyItem(executorWrapper, notifyItemEnum);
        if (Objects.isNull(notifyItem) || !AlarmManager.satisfyBaseCondition(notifyItem)) {
            return;
        }

        boolean ifAlarm = AlarmLimiter.ifAlarm(executorWrapper.getThreadPoolName(), notifyItemEnum.getValue());
        if (!ifAlarm) {
            log.debug("DynamicTp notify, alarm limit, dtpName: {}, notifyItem: {}",
                    executorWrapper.getThreadPoolName(), notifyItemEnum.getValue());
            return;
        }

        if (!AlarmManager.checkThreshold(executorWrapper, notifyItemEnum, notifyItem)) {
            return;
        }
        synchronized (SEND_LOCK) {
            // recheck alarm limit.
            ifAlarm = AlarmLimiter.ifAlarm(executorWrapper.getThreadPoolName(), notifyItemEnum.getValue());
            if (!ifAlarm) {
                log.warn("DynamicTp notify, concurrent send, alarm limit, dtpName: {}, notifyItem: {}",
                        executorWrapper.getThreadPoolName(), notifyItemEnum.getValue());
                return;
            }
            AlarmLimiter.putVal(executorWrapper.getThreadPoolName(), notifyItemEnum.getValue());
        }
        nextFilter.invoke(context);
    }
}
