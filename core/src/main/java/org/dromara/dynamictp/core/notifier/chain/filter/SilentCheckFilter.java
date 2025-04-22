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

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.pattern.filter.Invoker;
import org.dromara.dynamictp.core.notifier.alarm.AlarmLimiter;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SilentCheckFilter related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class SilentCheckFilter implements NotifyFilter {

    private static final Map<String, Lock> LOCK_MAP = new ConcurrentHashMap<>();

    @Override
    public int getOrder() {
        return 5;
    }

    @Override
    public void doFilter(BaseNotifyCtx context, Invoker<BaseNotifyCtx> nextInvoker) {
        if (isSilent(context)) {
            return;
        }
        nextInvoker.invoke(context);
    }

    protected boolean isSilent(BaseNotifyCtx context) {
        ExecutorWrapper executorWrapper = context.getExecutorWrapper();
        NotifyItem notifyItem = context.getNotifyItem();
        String lockKey = executorWrapper.getThreadPoolName();
        Lock lock = LOCK_MAP.computeIfAbsent(lockKey, k -> new ReentrantLock());

        lock.lock();
        try {
            boolean isAllowed = AlarmLimiter.isAllowed(executorWrapper.getThreadPoolName(), notifyItem.getType());
            if (!isAllowed) {
                if (log.isDebugEnabled()) {
                    log.debug("DynamicTp notify, trigger rate limit, threadPoolName: {}, notifyItem: {}",
                            executorWrapper.getThreadPoolName(), notifyItem.getType());
                }
                return true;
            }
            AlarmLimiter.putVal(executorWrapper.getThreadPoolName(), notifyItem.getType());
        } finally {
            lock.unlock();
        }
        return false;
    }
}
