package com.dtp.core.notify;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.dto.*;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.context.DtpContext;
import com.dtp.core.context.DtpContextHolder;
import com.dtp.core.thread.DtpExecutor;
import com.google.common.base.Joiner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.dtp.common.constant.DynamicTpConst.UNKNOWN;
import static com.dtp.common.constant.LarkNotifyConst.*;
import static com.dtp.core.notify.NotifyHelper.getAlarmKeys;
import static com.dtp.core.notify.NotifyHelper.getAllAlarmKeys;

/**
 * AbstractNotifier related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public abstract class AbstractNotifier implements Notifier {

    private static Instance instance;

    public Instance getInstance() {
        return instance;
    }

    protected AbstractNotifier() {
        init();
    }

    @SneakyThrows
    public static void init() {
        Environment environment = ApplicationContextHolder.getEnvironment();

        String appName = environment.getProperty("spring.application.name");
        appName = StringUtils.isNoneBlank(appName) ? appName : "application";

        String portStr = environment.getProperty("server.port");
        int port = StringUtils.isNotBlank(portStr) ? Integer.parseInt(portStr) : 0;

        String address = InetAddress.getLocalHost().getHostAddress();

        String[] profiles = environment.getActiveProfiles();
        if (profiles.length < 1) {
            profiles = environment.getDefaultProfiles();
        }
        instance = new Instance(address, port, appName, profiles[0]);
    }

    public String buildAlarmContent(NotifyPlatform platform, NotifyTypeEnum typeEnum, String template) {
        DtpContext context = DtpContextHolder.get();
        String threadPoolName = context.getExecutorWrapper().getThreadPoolName();
        val executor = (ThreadPoolExecutor) context.getExecutorWrapper().getExecutor();
        NotifyItem notifyItem = context.getNotifyItem();
        AlarmInfo alarmInfo = context.getAlarmInfo();

        val triple = AlarmCounter.countRrq(threadPoolName, executor);
        String receivesStr = getReceives(platform.getPlatform(), platform.getReceivers());

        String content = String.format(
                template,
                getInstance().getServiceName(),
                getInstance().getIp() + ":" + getInstance().getPort(),
                getInstance().getEnv(),
                populatePoolName(threadPoolName, executor),
                typeEnum.getValue(),
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
                triple.getLeft(),
                triple.getMiddle(),
                triple.getRight(),
                alarmInfo.getLastAlarmTime() == null ? UNKNOWN : alarmInfo.getLastAlarmTime(),
                DateUtil.now(),
                receivesStr,
                notifyItem.getInterval()
        );
        return highlightAlarmContent(content, typeEnum);
    }

    public String buildNoticeContent(NotifyPlatform platform,
                                     String template,
                                     DtpMainProp oldProp,
                                     List<String> diffs) {
        String threadPoolName = oldProp.getThreadPoolName();
        DtpContext context = DtpContextHolder.get();
        val executor = (ThreadPoolExecutor) context.getExecutorWrapper().getExecutor();
        String receivesStr = getReceives(platform.getPlatform(), platform.getReceivers());

        String content = String.format(
                template,
                getInstance().getServiceName(),
                getInstance().getIp() + ":" + getInstance().getPort(),
                getInstance().getEnv(),
                populatePoolName(threadPoolName, executor),
                oldProp.getCorePoolSize(),
                executor.getCorePoolSize(),
                oldProp.getMaxPoolSize(),
                executor.getMaximumPoolSize(),
                oldProp.isAllowCoreThreadTimeOut(),
                executor.allowsCoreThreadTimeOut(),
                oldProp.getKeepAliveTime(),
                executor.getKeepAliveTime(TimeUnit.SECONDS),
                executor.getQueue().getClass().getSimpleName(),
                oldProp.getQueueCapacity(),
                getQueueCapacity(executor),
                oldProp.getRejectType(),
                getRejectHandlerName(executor),
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
                    .map(receive -> StringUtils.startsWith(receive, LARK_OPENID_PREFIX) ?
                            String.format(LARK_AT_FORMAT_OPENID, receive) : String.format(LARK_AT_FORMAT_USERNAME, receive))
                    .collect(Collectors.joining(" "));
        } else {
            String[] receivers = StringUtils.split(receives, ',');
            return Joiner.on(", @").join(receivers);
        }
    }

    /**
     * Implement by subclass, get content color config.
     *
     * @return left: highlight color, right: other content color
     */
    protected abstract Pair<String, String> getColors();

    private String populatePoolName(String poolName, ThreadPoolExecutor executor) {

        String poolAlisaName = null;
        if (executor instanceof DtpExecutor) {
            poolAlisaName = ((DtpExecutor) executor).getTheadPoolAliasName();
        }
        if (StringUtils.isBlank(poolAlisaName)) {
            return poolName;
        }
        return poolName + "("+poolAlisaName+")";
    }

    private String getRejectHandlerName(ThreadPoolExecutor executor) {
        if (executor instanceof DtpExecutor) {
            return ((DtpExecutor) executor).getRejectHandlerName();
        }
        return executor.getRejectedExecutionHandler().getClass().getSimpleName();
    }

    private int getQueueCapacity(ThreadPoolExecutor executor) {
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
        for (Field field : DtpMainProp.getMainProps()) {
            content = content.replace(field.getName(), pair.getRight());
        }
        return content;
    }

    private String highlightAlarmContent(String content, NotifyTypeEnum typeEnum) {
        if (StringUtils.isBlank(content)) {
            return content;
        }

        Set<String> colorKeys = getAlarmKeys(typeEnum);
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
