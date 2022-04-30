package com.dtp.core.notify;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.dto.*;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.DtpRegistry;
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
        DtpContext contextWrapper = DtpContextHolder.get();
        String dtpName = contextWrapper.getDtpExecutor().getThreadPoolName();
        DtpExecutor executor = DtpRegistry.getDtpExecutor(dtpName);

        String receivesStr = getReceives(platform.getPlatform(), platform.getReceivers());

        NotifyItem notifyItem = contextWrapper.getNotifyItem();
        AlarmInfo alarmInfo = contextWrapper.getAlarmInfo();
        val triple = AlarmCounter.countNotifyItems(dtpName);
        String content = String.format(
                template,
                getInstance().getServiceName(),
                getInstance().getIp() + ":" + getInstance().getPort(),
                getInstance().getEnv(),
                executor.getThreadPoolName(),
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
                executor.getQueueName(),
                executor.getQueueCapacity(),
                executor.getQueue().size(),
                executor.getQueue().remainingCapacity(),
                executor.getRejectHandlerName(),
                triple.getLeft() + "/" + executor.getRejectCount(),
                triple.getMiddle() + "/" + executor.getRunTimeoutCount(),
                triple.getRight() + "/" + executor.getQueueTimeoutCount(),
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
        String threadPoolName = oldProp.getDtpName();
        DtpExecutor dtpExecutor = DtpRegistry.getDtpExecutor(threadPoolName);

        String receivesStr = getReceives(platform.getPlatform(), platform.getReceivers());

        String content = String.format(
                template,
                getInstance().getServiceName(),
                getInstance().getIp() + ":" + getInstance().getPort(),
                getInstance().getEnv(),
                threadPoolName,
                oldProp.getCorePoolSize(),
                dtpExecutor.getCorePoolSize(),
                oldProp.getMaxPoolSize(),
                dtpExecutor.getMaximumPoolSize(),
                oldProp.isAllowCoreThreadTimeOut(),
                dtpExecutor.allowsCoreThreadTimeOut(),
                oldProp.getKeepAliveTime(),
                dtpExecutor.getKeepAliveTime(TimeUnit.SECONDS),
                dtpExecutor.getQueueName(),
                oldProp.getQueueCapacity(),
                dtpExecutor.getQueueCapacity(),
                oldProp.getRejectType(),
                dtpExecutor.getRejectHandlerName(),
                receivesStr,
                DateTime.now()
        );
        return highlightNotifyContent(content, diffs);
    }

    private String getReceives(String platform, String receives) {
        if (StrUtil.isBlank(receives)) {
            return "";
        }
        if (NotifyPlatformEnum.LARK.name().toLowerCase().equals(platform)) {
            return Arrays.stream(receives.split(","))
                    .map(receive -> StrUtil.startWith(receive, LARK_OPENID_PREFIX) ? String.format(LARK_AT_FORMAT_OPENID, receive) : String.format(LARK_AT_FORMAT_USERNAME, receive))
                    .collect(Collectors.joining(" "));
        } else {
            List<String> receivers = StrUtil.split(receives, ',');
            return Joiner.on(", @").join(receivers);
        }
    }

    /**
     * Implement by subclass, get content color config.
     *
     * @return left: highlight color, right: other content color
     */
    protected abstract Pair<String, String> getColors();

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
