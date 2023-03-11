package com.dtp.core.notify.filter;

import com.dtp.core.support.ExecutorWrapper;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.pattern.filter.Invoker;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.notify.alarm.AlarmLimiter;
import com.dtp.core.notify.manager.AlarmManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;

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
    public void doFilter(BaseNotifyCtx context, Invoker<BaseNotifyCtx> nextInvoker) {

        val executorWrapper = context.getExecutorWrapper();
        val notifyItem = context.getNotifyItem();
        if (Objects.isNull(notifyItem) || !satisfyBaseCondition(notifyItem, executorWrapper)) {
            return;
        }

        boolean ifAlarm = AlarmLimiter.ifAlarm(executorWrapper.getThreadPoolName(), notifyItem.getType());
        if (!ifAlarm) {
            log.debug("DynamicTp notify, alarm limit, threadPoolName: {}, notifyItem: {}",
                    executorWrapper.getThreadPoolName(), notifyItem.getType());
            return;
        }

        if (!AlarmManager.checkThreshold(executorWrapper, context.getNotifyItemEnum(), notifyItem)) {
            return;
        }
        synchronized (SEND_LOCK) {
            // recheck alarm limit.
            ifAlarm = AlarmLimiter.ifAlarm(executorWrapper.getThreadPoolName(), notifyItem.getType());
            if (!ifAlarm) {
                log.warn("DynamicTp notify, concurrent send, alarm limit, threadPoolName: {}, notifyItem: {}",
                        executorWrapper.getThreadPoolName(), notifyItem.getType());
                return;
            }
            AlarmLimiter.putVal(executorWrapper.getThreadPoolName(), notifyItem.getType());
        }
        nextInvoker.invoke(context);
    }

    private boolean satisfyBaseCondition(NotifyItem notifyItem, ExecutorWrapper executor) {
        return executor.isNotifyEnabled()
                && notifyItem.isEnabled()
                && CollectionUtils.isNotEmpty(notifyItem.getPlatformIds());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
