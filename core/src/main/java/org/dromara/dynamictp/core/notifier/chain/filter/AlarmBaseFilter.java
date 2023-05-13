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

import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.pattern.filter.Invoker;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.alarm.AlarmLimiter;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;

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
