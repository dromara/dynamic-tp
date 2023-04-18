package com.dtp.core.notifier;

import com.dtp.common.constant.DynamicTpConst;
import com.dtp.common.constant.LarkNotifyConst;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.common.entity.AlarmInfo;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.entity.NotifyPlatform;
import com.dtp.common.entity.TpMainFields;
import com.dtp.common.util.CommonUtil;
import com.dtp.common.util.DateUtil;
import com.dtp.core.notifier.context.AlarmCtx;
import com.dtp.core.notifier.context.BaseNotifyCtx;
import com.dtp.core.notifier.context.DtpNotifyCtxHolder;
import com.dtp.core.notifier.alarm.AlarmCounter;
import com.dtp.core.notifier.base.Notifier;
import com.dtp.core.notifier.capture.CapturedBlockingQueue;
import com.dtp.core.support.ExecutorAdapter;
import com.dtp.core.support.ExecutorWrapper;
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

import static com.dtp.core.notifier.manager.NotifyHelper.getAlarmKeys;
import static com.dtp.core.notifier.manager.NotifyHelper.getAllAlarmKeys;

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
        val executor = executorWrapper.getExecutor();
        NotifyItem notifyItem = context.getNotifyItem();
        val alarmCounter = AlarmCounter.countStrRrq(executorWrapper.getThreadPoolName(), executor);

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
                executor.getQueueSize(),
                getQueueName(executor),
                executor.getQueueCapacity(),
                executor.getQueueSize(),
                executor.getQueueRemainingCapacity(),
                executor.getRejectHandlerType(),
                alarmCounter.getLeft(),
                alarmCounter.getMiddle(),
                alarmCounter.getRight(),
                Optional.ofNullable(context.getAlarmInfo()).map(AlarmInfo::getLastAlarmTime).orElse(DynamicTpConst.UNKNOWN),
                DateUtil.now(),
                getReceives(platform.getPlatform(), platform.getReceivers()),
                Optional.ofNullable(MDC.get(DynamicTpConst.TRACE_ID)).orElse(DynamicTpConst.UNKNOWN),
                notifyItem.getInterval()
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
                getQueueName(executor),
                oldFields.getQueueCapacity(), executor.getQueueCapacity(),
                oldFields.getRejectType(), executor.getRejectHandlerType(),
                getReceives(platform.getPlatform(), platform.getReceivers()),
                DateUtil.now()
        );
        return highlightNotifyContent(content, diffs);
    }

    private String getReceives(String platform, String receives) {
        if (StringUtils.isBlank(receives)) {
            return "";
        }
        if (NotifyPlatformEnum.LARK.name().toLowerCase().equals(platform)) {
            return Arrays.stream(receives.split(","))
                    .map(receive -> StringUtils.startsWith(receive, LarkNotifyConst.LARK_OPENID_PREFIX)
                            ? String.format(LarkNotifyConst.LARK_AT_FORMAT_OPENID, receive) : String.format(LarkNotifyConst.LARK_AT_FORMAT_USERNAME, receive))
                    .collect(Collectors.joining(" "));
        } else {
            String[] receivers = StringUtils.split(receives, ',');
            return Joiner.on(", @").join(receivers);
        }
    }

    protected String populatePoolName(ExecutorWrapper executorWrapper) {
        String poolAlisaName = executorWrapper.getThreadPoolAliasName();
        if (StringUtils.isBlank(poolAlisaName)) {
            return executorWrapper.getThreadPoolName();
        }
        return executorWrapper.getThreadPoolName() + "(" + poolAlisaName + ")";
    }

    protected String getQueueName(ExecutorAdapter<?> executor) {
        if (executor.getQueue() instanceof CapturedBlockingQueue) {
            return ((CapturedBlockingQueue) executor.getQueue()).getOriginQueue().getClass().getSimpleName();
        }
        return executor.getQueue().getClass().getSimpleName();
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
