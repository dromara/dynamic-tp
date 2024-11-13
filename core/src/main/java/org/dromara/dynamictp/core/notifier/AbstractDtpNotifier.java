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

package org.dromara.dynamictp.core.notifier;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.AlarmInfo;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.common.notifier.Notifier;
import org.dromara.dynamictp.common.util.BeanCopierUtil;
import org.dromara.dynamictp.common.util.CommonUtil;
import org.dromara.dynamictp.common.util.DateUtil;
import org.dromara.dynamictp.core.notifier.alarm.AlarmCounter;
import org.dromara.dynamictp.core.notifier.context.AlarmCtx;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.system.SystemMetricManager;
import org.slf4j.MDC;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.UNKNOWN;
import static org.dromara.dynamictp.core.notifier.manager.NotifyHelper.getAlarmKeys;
import static org.dromara.dynamictp.core.notifier.manager.NotifyHelper.getAllAlarmKeys;

/**
 * AbstractDtpNotifier related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public abstract class AbstractDtpNotifier implements DtpNotifier {

    protected Notifier notifier;

    protected AbstractDtpNotifier() { }

    protected AbstractDtpNotifier(Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void sendChangeMsg(NotifyPlatform notifyPlatform, TpMainFields oldFields, List<String> diffs) {
        String content = buildNoticeContent(notifyPlatform, oldFields, diffs);
        if (StringUtils.isBlank(content)) {
            log.debug("Notice content is empty, ignore send notice message.");
            return;
        }
        notifier.send(newTargetPlatform(notifyPlatform), content);
    }

    @Override
    public void sendAlarmMsg(NotifyPlatform notifyPlatform, NotifyItemEnum notifyItemEnum) {
        String content = buildAlarmContent(notifyPlatform, notifyItemEnum);
        if (StringUtils.isBlank(content)) {
            log.debug("Alarm content is empty, ignore send alarm message.");
            return;
        }
        notifier.send(newTargetPlatform(notifyPlatform), content);
    }

    protected String buildAlarmContent(NotifyPlatform platform, NotifyItemEnum notifyItemEnum) {
        AlarmCtx context = (AlarmCtx) DtpNotifyCtxHolder.get();
        ExecutorWrapper executorWrapper = context.getExecutorWrapper();
        val executor = executorWrapper.getExecutor();
        NotifyItem notifyItem = context.getNotifyItem();
        val statProvider = executorWrapper.getThreadPoolStatProvider();
        val alarmValue = notifyItem.getThreshold() + notifyItemEnum.getUnit() + " / "
                + AlarmCounter.calcCurrentValue(executorWrapper, notifyItemEnum) + notifyItemEnum.getUnit();
        String content = String.format(
                getAlarmTemplate(),
                CommonUtil.getInstance().getServiceName(),
                CommonUtil.getInstance().getIp() + ":" + CommonUtil.getInstance().getPort(),
                CommonUtil.getInstance().getEnv(),
                populatePoolName(executorWrapper),
                populateAlarmItem(notifyItemEnum, executorWrapper),
                alarmValue,
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getPoolSize(),
                executor.getActiveCount(),
                executor.getLargestPoolSize(),
                executor.getTaskCount(),
                executor.getCompletedTaskCount(),
                executor.getQueueSize(),
                executor.getQueueType(),
                executor.getQueueCapacity(),
                executor.getQueueSize(),
                executor.getQueueRemainingCapacity(),
                executor.getRejectHandlerType(),
                statProvider.getRejectedTaskCount(),
                statProvider.getRunTimeoutCount(),
                statProvider.getQueueTimeoutCount(),
                Optional.ofNullable(context.getAlarmInfo()).map(AlarmInfo::getLastAlarmTime).orElse(UNKNOWN),
                DateUtil.now(),
                getReceives(notifyItem, platform),
                getTraceInfo(),
                notifyItem.getInterval(),
                getExtInfo()
        );
        return highlightAlarmContent(content, notifyItemEnum);
    }

    protected String buildNoticeContent(NotifyPlatform platform, TpMainFields oldFields, List<String> diffs) {
        BaseNotifyCtx context = DtpNotifyCtxHolder.get();
        ExecutorWrapper executorWrapper = context.getExecutorWrapper();
        val executor = executorWrapper.getExecutor();

        String content = String.format(
                getNoticeTemplate(),
                CommonUtil.getInstance().getServiceName(),
                CommonUtil.getInstance().getIp() + ":" + CommonUtil.getInstance().getPort(),
                CommonUtil.getInstance().getEnv(),
                populatePoolName(executorWrapper),
                oldFields.getCorePoolSize(), executor.getCorePoolSize(),
                oldFields.getMaxPoolSize(), executor.getMaximumPoolSize(),
                oldFields.isAllowCoreThreadTimeOut(), executor.allowsCoreThreadTimeOut(),
                oldFields.getKeepAliveTime(), executor.getKeepAliveTime(TimeUnit.SECONDS),
                executor.getQueueType(),
                oldFields.getQueueCapacity(), executor.getQueueCapacity(),
                oldFields.getRejectType(), executor.getRejectHandlerType(),
                getReceives(context.getNotifyItem(), platform),
                DateUtil.now()
        );
        return highlightNotifyContent(content, diffs);
    }

    protected String getTraceInfo() {
        String tid = MDC.get(TRACE_ID);
        if (StringUtils.isBlank(tid)) {
            return UNKNOWN;
        }
        return tid;
    }

    protected String getExtInfo() {
        return SystemMetricManager.getSystemMetric();
    }

    protected String getReceives(NotifyItem notifyItem, NotifyPlatform platform) {
        String receives = StringUtils.isBlank(notifyItem.getReceivers()) ?
                platform.getReceivers() : notifyItem.getReceivers();
        if (StringUtils.isBlank(receives)) {
            return StringUtils.EMPTY;
        }
        return formatReceivers(receives);
    }

    protected String formatReceivers(String receives) {
        String[] receivers = StringUtils.split(receives, ',');
        return Joiner.on(", @").join(receivers);
    }

    private NotifyPlatform newTargetPlatform(NotifyPlatform platform) {
        NotifyPlatform targetPlatform = new NotifyPlatform();
        BeanCopierUtil.copyProperties(platform, targetPlatform);

        BaseNotifyCtx context = DtpNotifyCtxHolder.get();
        NotifyItem item = context.getNotifyItem();
        String receives = StringUtils.isBlank(item.getReceivers()) ? platform.getReceivers() : item.getReceivers();
        targetPlatform.setReceivers(receives);
        return targetPlatform;
    }

    protected String populatePoolName(ExecutorWrapper executorWrapper) {
        String poolAlisaName = executorWrapper.getThreadPoolAliasName();
        if (StringUtils.isBlank(poolAlisaName)) {
            return executorWrapper.getThreadPoolName();
        }
        return executorWrapper.getThreadPoolName() + " (" + poolAlisaName + ")";
    }

    protected String populateAlarmItem(NotifyItemEnum notifyType, ExecutorWrapper executorWrapper) {
        String suffix = StringUtils.EMPTY;
        if (notifyType == NotifyItemEnum.RUN_TIMEOUT) {
            suffix = " (" + executorWrapper.getThreadPoolStatProvider().getRunTimeout() + "ms)";
        } else if (notifyType == NotifyItemEnum.QUEUE_TIMEOUT) {
            suffix = " (" + executorWrapper.getThreadPoolStatProvider().getQueueTimeout() + "ms)";
        }
        return notifyType.getValue() + suffix;
    }

    private String highlightNotifyContent(String content, List<String> diffs) {
        if (StringUtils.isBlank(content) || Objects.isNull(getColors())) {
            return content;
        }

        Pair<String, String> pair = getColors();
        for (String field : diffs) {
            content = content.replace(field, pair.getLeft());
        }
        for (Field field : TpMainFields.getMainFields()) {
            content = content.replace(field.getName(), pair.getRight());
        }
        return content;
    }

    private String highlightAlarmContent(String content, NotifyItemEnum notifyItemEnum) {
        if (StringUtils.isBlank(content) || Objects.isNull(getColors())) {
            return content;
        }

        Set<String> colorKeys = getAlarmKeys(notifyItemEnum);
        Pair<String, String> pair = getColors();
        for (String field : colorKeys) {
            content = content.replace(field, pair.getLeft());
        }
        for (String field : getAllAlarmKeys()) {
            content = content.replace(field, pair.getRight());
        }
        return content;
    }

    /**
     * Implement by subclass, get notice template.
     *
     * @return notice template
     */
    protected abstract String getNoticeTemplate();

    /**
     * Implement by subclass, get alarm template.
     *
     * @return alarm template
     */
    protected abstract String getAlarmTemplate();

    /**
     * Implement by subclass, get content color config.
     *
     * @return left: highlight color, right: other content color
     */
    protected abstract Pair<String, String> getColors();
}
