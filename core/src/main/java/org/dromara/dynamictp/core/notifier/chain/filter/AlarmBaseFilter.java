/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.core.notifier.chain.filter;

import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.AlarmInfo;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.pattern.filter.Invoker;
import org.dromara.dynamictp.core.notifier.alarm.AlarmCounter;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

import java.util.Objects;

/**
 * AlarmBaseFilter related
 *
 * @author yanhom
 * @since 1.0.8
 **/
@Slf4j
public class AlarmBaseFilter implements NotifyFilter {

    @Override
    public void doFilter(BaseNotifyCtx context, Invoker<BaseNotifyCtx> nextInvoker) {
        val executorWrapper = context.getExecutorWrapper();
        val notifyItem = context.getNotifyItem();
        if (Objects.isNull(notifyItem) || !satisfyBaseCondition(notifyItem, executorWrapper)) {
            return;
        }

        if (!hasReachedThreshold(executorWrapper, context.getNotifyItemEnum(), notifyItem)) {
            return;
        }
        AlarmCounter.incAlarmCount(executorWrapper.getThreadPoolName(), notifyItem.getType());
        int count = AlarmCounter.getCount(executorWrapper.getThreadPoolName(), notifyItem.getType());
        if (count < notifyItem.getCount()) {
            return;
        }
        nextInvoker.invoke(context);
    }

    private boolean satisfyBaseCondition(NotifyItem notifyItem, ExecutorWrapper executor) {
        return executor.isNotifyEnabled()
                && notifyItem.isEnabled()
                && CollectionUtils.isNotEmpty(notifyItem.getPlatformIds());
    }

    private boolean hasReachedThreshold(ExecutorWrapper executor, NotifyItemEnum notifyType, NotifyItem notifyItem) {
        switch (notifyType) {
            case CAPACITY:
                return checkCapacity(executor, notifyItem);
            case LIVENESS:
                return checkLiveness(executor, notifyItem);
            case REJECT:
            case RUN_TIMEOUT:
            case QUEUE_TIMEOUT:
                return true;
            default:
                log.error("Unsupported alarm type [{}]", notifyType);
                return false;
        }
    }

    private boolean checkLiveness(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {
        val executor = executorWrapper.getExecutor();
        int maximumPoolSize = executor.getMaximumPoolSize();
        double div = NumberUtil.div(executor.getActiveCount(), maximumPoolSize, 2) * 100;
        return div >= notifyItem.getThreshold();
    }

    private boolean checkCapacity(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {
        val executor = executorWrapper.getExecutor();
        if (executor.getQueueSize() <= 0) {
            return false;
        }
        double div = NumberUtil.div(executor.getQueueSize(), executor.getQueueCapacity(), 2) * 100;
        return div >= notifyItem.getThreshold();
    }

    private static boolean checkWithAlarmInfo(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {
        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executorWrapper.getThreadPoolName(), notifyItem.getType());
        return alarmInfo.getCount() >= notifyItem.getThreshold();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
