package com.dtp.core.notify;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.common.entity.AlarmInfo;
import com.dtp.common.entity.TpMainFields;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.entity.NotifyPlatform;
import com.dtp.common.util.CommonUtil;
import com.dtp.core.context.AlarmCtx;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.context.DtpNotifyCtxHolder;
import com.dtp.core.notify.alarm.AlarmCounter;
import com.dtp.core.notify.base.Notifier;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.support.ExecutorAdapter;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.dtp.common.constant.DynamicTpConst.TRACE_ID;
import static com.dtp.common.constant.DynamicTpConst.UNKNOWN;
import static com.dtp.common.constant.LarkNotifyConst.LARK_AT_FORMAT_OPENID;
import static com.dtp.common.constant.LarkNotifyConst.LARK_AT_FORMAT_USERNAME;
import static com.dtp.common.constant.LarkNotifyConst.LARK_OPENID_PREFIX;
import static com.dtp.core.notify.manager.NotifyHelper.getAlarmKeys;
import static com.dtp.core.notify.manager.NotifyHelper.getAllAlarmKeys;

/**
 * AbstractDtpNotifier related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public abstract class AbstractDtpNotifier implements DtpNotifier {

    protected Notifier notifier;

    protected AbstractDtpNotifier() {
    }

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
        notifier.send(notifyPlatform, content);
    }

    @Override
    public void sendAlarmMsg(NotifyPlatform notifyPlatform, NotifyItemEnum notifyItemEnum) {
        String content = buildAlarmContent(notifyPlatform, notifyItemEnum);
        if (StringUtils.isBlank(content)) {
            log.debug("Alarm content is empty, ignore send alarm message.");
            return;
        }
        notifier.send(notifyPlatform, content);
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

    protected String buildAlarmContent(NotifyPlatform platform, NotifyItemEnum notifyItemEnum) {
        AlarmCtx context = (AlarmCtx) DtpNotifyCtxHolder.get();
        ExecutorWrapper executorWrapper = context.getExecutorWrapper();
        String threadPoolName = executorWrapper.getThreadPoolName();
        val executor = executorWrapper.getExecutor();
        NotifyItem notifyItem = context.getNotifyItem();
        AlarmInfo alarmInfo = context.getAlarmInfo();

        val alarmCounter = AlarmCounter.countStrRrq(threadPoolName, executor);
        String receivesStr = getReceives(platform.getPlatform(), platform.getReceivers());

        String content = String.format(
                getAlarmTemplate(),
                CommonUtil.getInstance().getServiceName(),
                CommonUtil.getInstance().getIp() + ":" + CommonUtil.getInstance().getPort(),
                CommonUtil.getInstance().getEnv(),
                populatePoolName(executorWrapper),
                notifyItemEnum.getValue(),
                notifyItem.getThreshold(),
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getPoolSize(),
                executor.getActiveCount(),
                executor.getLargestPoolSize(),
                executor.getTaskCount(),
                executor.getCompletedTaskCount(),
                executor.getQueue().size(),
                executor.getQueue().getClass().getSimpleName(),
                getQueueCapacity(executor),
                executor.getQueue().size(),
                executor.getQueue().remainingCapacity(),
                getRejectHandlerName(executor),
                alarmCounter.getLeft(),
                alarmCounter.getMiddle(),
                alarmCounter.getRight(),
                Optional.ofNullable(alarmInfo.getLastAlarmTime()).orElse(UNKNOWN),
                DateUtil.now(),
                receivesStr,
                Optional.ofNullable(MDC.get(TRACE_ID)).orElse(UNKNOWN),
                notifyItem.getInterval()
        );
        return highlightAlarmContent(content, notifyItemEnum);
    }

    protected String buildNoticeContent(NotifyPlatform platform, TpMainFields oldFields, List<String> diffs) {
        BaseNotifyCtx context = DtpNotifyCtxHolder.get();
        ExecutorWrapper executorWrapper = context.getExecutorWrapper();
        val executor = executorWrapper.getExecutor();
        String receivesStr = getReceives(platform.getPlatform(), platform.getReceivers());

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
                executor.getQueue().getClass().getSimpleName(),
                oldFields.getQueueCapacity(), getQueueCapacity(executor),
                oldFields.getRejectType(), getRejectHandlerName(executor),
                receivesStr,
                DateTime.now()
        );
        return highlightNotifyContent(content, diffs);
    }

    private String getReceives(String platform, String receives) {
        if (StringUtils.isBlank(receives)) {
            return "";
        }
        if (NotifyPlatformEnum.LARK.name().toLowerCase().equals(platform)) {
            return Arrays.stream(receives.split(","))
                    .map(receive -> StringUtils.startsWith(receive, LARK_OPENID_PREFIX)
                            ? String.format(LARK_AT_FORMAT_OPENID, receive) : String.format(LARK_AT_FORMAT_USERNAME, receive))
                    .collect(Collectors.joining(" "));
        } else {
            String[] receivers = StringUtils.split(receives, ',');
            return Joiner.on(", @").join(receivers);
        }
    }

    protected String populatePoolName(ExecutorWrapper executorWrapper) {

        String poolAlisaName;
        if (executorWrapper.getExecutor() instanceof DtpExecutor) {
            poolAlisaName = ((DtpExecutor) executorWrapper.getExecutor()).getThreadPoolAliasName();
        } else {
            poolAlisaName = executorWrapper.getThreadPoolAliasName();
        }
        if (StringUtils.isBlank(poolAlisaName)) {
            return executorWrapper.getThreadPoolName();
        }
        return executorWrapper.getThreadPoolName() + "(" + poolAlisaName + ")";
    }

    protected String getRejectHandlerName(ExecutorAdapter<?> executor) {
        return executor.getRejectHandlerName();
    }

    protected int getQueueCapacity(ExecutorAdapter<?> executor) {
        if (executor instanceof DtpExecutor) {
            return ((DtpExecutor) executor).getQueueCapacity();
        }
        return executor.getQueue().size() + executor.getQueue().remainingCapacity();
    }

    private String highlightNotifyContent(String content, List<String> diffs) {
        if (StringUtils.isBlank(content)) {
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
        if (StringUtils.isBlank(content)) {
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
}
