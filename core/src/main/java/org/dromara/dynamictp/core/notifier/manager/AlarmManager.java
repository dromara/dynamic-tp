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

package org.dromara.dynamictp.core.notifier.manager;

import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.em.RejectedTypeEnum;
import org.dromara.dynamictp.common.entity.AlarmInfo;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.pattern.filter.InvokerChain;
import org.dromara.dynamictp.core.notifier.alarm.AlarmCounter;
import org.dromara.dynamictp.core.notifier.alarm.AlarmLimiter;
import org.dromara.dynamictp.core.notifier.context.AlarmCtx;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrappers;
import org.slf4j.MDC;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;
import static org.dromara.dynamictp.common.em.QueueTypeEnum.LINKED_BLOCKING_QUEUE;

/**
 * AlarmManager related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class AlarmManager {

    private static final ExecutorService ALARM_EXECUTOR = ThreadPoolBuilder.newBuilder()
            .threadFactory("dtp-alarm")
            .corePoolSize(1)
            .maximumPoolSize(1)
            .workQueue(LINKED_BLOCKING_QUEUE.getName(), 2000)
            .rejectedExecutionHandler(RejectedTypeEnum.DISCARD_OLDEST_POLICY.getName())
            .rejectEnhanced(false)
            .taskWrappers(TaskWrappers.getInstance().getByNames(Sets.newHashSet("mdc")))
            .buildDynamic();

    private static final InvokerChain<BaseNotifyCtx> ALARM_INVOKER_CHAIN;

    static {
        ALARM_INVOKER_CHAIN = NotifyFilterBuilder.getAlarmInvokerChain();
    }

    private AlarmManager() { }

    public static void initAlarm(String poolName, List<NotifyItem> notifyItems) {
        notifyItems.forEach(x -> initAlarm(poolName, x));
    }

    public static void initAlarm(String poolName, NotifyItem notifyItem) {
        AlarmLimiter.initAlarmLimiter(poolName, notifyItem);
        AlarmCounter.init(poolName, notifyItem.getType());
    }

    public static void tryAlarmAsync(ExecutorWrapper executorWrapper, NotifyItemEnum notifyType, Runnable runnable) {
        preAlarm(runnable);
        try {
            ALARM_EXECUTOR.execute(() -> doTryAlarm(executorWrapper, notifyType));
        } finally {
            postAlarm(runnable);
        }
    }

    public static void tryAlarmAsync(ExecutorWrapper executorWrapper, List<NotifyItemEnum> notifyTypes) {
        ALARM_EXECUTOR.execute(() -> notifyTypes.forEach(x -> doTryAlarm(executorWrapper, x)));
    }

    public static void doTryAlarm(ExecutorWrapper executorWrapper, NotifyItemEnum notifyType) {
        AlarmCounter.incAlarmCounter(executorWrapper.getThreadPoolName(), notifyType.getValue());
        NotifyHelper.getNotifyItem(executorWrapper, notifyType).ifPresent(notifyItem -> {
            val alarmCtx = new AlarmCtx(executorWrapper, notifyItem);
            ALARM_INVOKER_CHAIN.proceed(alarmCtx);
        });
    }

    public static boolean checkThreshold(ExecutorWrapper executor, NotifyItemEnum notifyType, NotifyItem notifyItem) {

        switch (notifyType) {
            case CAPACITY:
                return checkCapacity(executor, notifyItem);
            case LIVENESS:
                return checkLiveness(executor, notifyItem);
            case REJECT:
            case RUN_TIMEOUT:
            case QUEUE_TIMEOUT:
                return checkWithAlarmInfo(executor, notifyItem);
            default:
                log.error("Unsupported alarm type [{}]", notifyType);
                return false;
        }
    }

    public static void destroy() {
        ALARM_EXECUTOR.shutdownNow();
    }

    private static boolean checkLiveness(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {
        val executor = executorWrapper.getExecutor();
        int maximumPoolSize = executor.getMaximumPoolSize();
        double div = NumberUtil.div(executor.getActiveCount(), maximumPoolSize, 2) * 100;
        return div >= notifyItem.getThreshold();
    }

    private static boolean checkCapacity(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {

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

    private static void preAlarm(Runnable runnable) {
        if (runnable instanceof DtpRunnable) {
            MDC.put(TRACE_ID, ((DtpRunnable) runnable).getTraceId());
        }
    }

    private static void postAlarm(Runnable runnable) {
        if (runnable instanceof DtpRunnable) {
            MDC.remove(TRACE_ID);
        }
    }
}
