package org.dromara.dynamictp.core.notifier.chain.filter;

import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.pattern.filter.Invoker;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Objects;

/**
 * NoticeBaseFilter related
 *
 * @author yanhom
 * @since 1.1.0
 **/
@Slf4j
public class NoticeBaseFilter implements NotifyFilter {

    @Override
    public void doFilter(BaseNotifyCtx context, Invoker<BaseNotifyCtx> nextInvoker) {

        val executorWrapper = context.getExecutorWrapper();
        val notifyItem = context.getNotifyItem();
        if (Objects.isNull(notifyItem) || !satisfyBaseCondition(notifyItem, executorWrapper)) {
            log.debug("DynamicTp notify, no platforms configured or notification is not enabled, threadPoolName: {}",
                    executorWrapper.getThreadPoolName());
            return;
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
