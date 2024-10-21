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

package org.dromara.dynamictp.extension.notify.email;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.em.NotifyPlatformEnum;
import org.dromara.dynamictp.common.entity.AlarmInfo;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.util.CommonUtil;
import org.dromara.dynamictp.common.util.DateUtil;
import org.dromara.dynamictp.core.notifier.AbstractDtpNotifier;
import org.dromara.dynamictp.core.notifier.alarm.AlarmCounter;
import org.dromara.dynamictp.core.notifier.context.AlarmCtx;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.UNKNOWN;
import static org.dromara.dynamictp.core.notifier.manager.NotifyHelper.getAlarmKeys;

/**
 * DtpEmailNotifier related
 *
 * @author ljinfeng
 * @since 1.0.8
 */
@Slf4j
public class DtpEmailNotifier extends AbstractDtpNotifier {

    public DtpEmailNotifier() {
        super(ContextManagerHelper.getBean(EmailNotifier.class));
    }

    @Override
    public String platform() {
        return NotifyPlatformEnum.EMAIL.name().toLowerCase();
    }

    @Override
    protected String getNoticeTemplate() {
        return null;
    }

    @Override
    protected String getAlarmTemplate() {
        return null;
    }

    @Override
    protected Pair<String, String> getColors() {
        return null;
    }

    @Override
    protected String buildAlarmContent(NotifyPlatform platform, NotifyItemEnum notifyItemEnum) {
        AlarmCtx alarmCtx = (AlarmCtx) DtpNotifyCtxHolder.get();
        ExecutorWrapper executorWrapper = alarmCtx.getExecutorWrapper();
        val executor = executorWrapper.getExecutor();
        NotifyItem notifyItem = alarmCtx.getNotifyItem();
        AlarmInfo alarmInfo = alarmCtx.getAlarmInfo();
        val statProvider = executorWrapper.getThreadPoolStatProvider();
        val alarmValue = notifyItem.getThreshold() + notifyItemEnum.getUnit() + " / "
                + AlarmCounter.calcCurrentValue(executorWrapper, notifyItemEnum) + notifyItemEnum.getUnit();

        Context context = newContext(executorWrapper);
        context.setVariable("alarmType", populateAlarmItem(notifyItemEnum, executorWrapper));
        context.setVariable("alarmValue", alarmValue);
        context.setVariable("corePoolSize", executor.getCorePoolSize());
        context.setVariable("maximumPoolSize", executor.getMaximumPoolSize());
        context.setVariable("poolSize", executor.getPoolSize());
        context.setVariable("activeCount", executor.getActiveCount());
        context.setVariable("largestPoolSize", executor.getLargestPoolSize());
        context.setVariable("taskCount", executor.getTaskCount());
        context.setVariable("completedTaskCount", executor.getCompletedTaskCount());
        context.setVariable("waitingTaskCount", executor.getQueueSize());
        context.setVariable("queueType", executor.getQueueType());
        context.setVariable("queueCapacity", executor.getQueueCapacity());
        context.setVariable("queueSize", executor.getQueueSize());
        context.setVariable("queueRemaining", executor.getQueueRemainingCapacity());
        context.setVariable("rejectType", executor.getRejectHandlerType());
        context.setVariable("rejectCount", statProvider.getRejectedTaskCount());
        context.setVariable("runTimeoutCount", statProvider.getRunTimeoutCount());
        context.setVariable("queueTimeoutCount", statProvider.getQueueTimeoutCount());
        context.setVariable("lastAlarmTime", alarmInfo.getLastAlarmTime() == null ? UNKNOWN : alarmInfo.getLastAlarmTime());
        context.setVariable("alarmTime", DateUtil.now());
        context.setVariable("trace", getTraceInfo());
        context.setVariable("alarmInterval", notifyItem.getInterval());
        context.setVariable("highlightVariables", getAlarmKeys(notifyItemEnum));
        context.setVariable("ext", getExtInfo());
        return ((EmailNotifier) notifier).processTemplateContent("alarm", context);
    }

    @Override
    protected String buildNoticeContent(NotifyPlatform platform, TpMainFields oldFields, List<String> diffs) {
        BaseNotifyCtx notifyCtx = DtpNotifyCtxHolder.get();
        ExecutorWrapper executorWrapper = notifyCtx.getExecutorWrapper();
        val executor = executorWrapper.getExecutor();

        Context context = newContext(executorWrapper);
        context.setVariable("oldCorePoolSize", oldFields.getCorePoolSize());
        context.setVariable("newCorePoolSize", executor.getCorePoolSize());
        context.setVariable("oldMaxPoolSize", oldFields.getMaxPoolSize());
        context.setVariable("newMaxPoolSize", executor.getMaximumPoolSize());
        context.setVariable("oldIsAllowCoreThreadTimeOut", oldFields.isAllowCoreThreadTimeOut());
        context.setVariable("newIsAllowCoreThreadTimeOut", executor.allowsCoreThreadTimeOut());
        context.setVariable("oldKeepAliveTime", oldFields.getKeepAliveTime());
        context.setVariable("newKeepAliveTime", executor.getKeepAliveTime(TimeUnit.SECONDS));
        context.setVariable("queueType", executor.getQueueType());
        context.setVariable("oldQueueCapacity", oldFields.getQueueCapacity());
        context.setVariable("newQueueCapacity", executor.getQueueCapacity());
        context.setVariable("oldRejectType", oldFields.getRejectType());
        context.setVariable("newRejectType", executor.getRejectHandlerType());
        context.setVariable("notifyTime", DateUtil.now());
        context.setVariable("diffs", diffs != null ? diffs : Collections.emptySet());
        return ((EmailNotifier) notifier).processTemplateContent("notice", context);
    }

    private Context newContext(ExecutorWrapper executorWrapper) {
        Context context = new Context();
        context.setVariable("serviceName", CommonUtil.getInstance().getServiceName());
        context.setVariable("serviceAddress", CommonUtil.getInstance().getIp() + ":" + CommonUtil.getInstance().getPort());
        context.setVariable("serviceEnv", CommonUtil.getInstance().getEnv());
        context.setVariable("poolName", populatePoolName(executorWrapper));
        return context;
    }
}
