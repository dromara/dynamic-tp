package com.dtp.core.notify;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.dto.DtpMainProp;
import com.dtp.common.dto.Instance;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.DtpRegistry;
import com.dtp.core.context.DtpContext;
import com.dtp.core.context.DtpContextHolder;
import com.dtp.core.thread.DtpExecutor;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.dtp.core.notify.NotifyHelper.*;

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
        DtpExecutor executor = DtpRegistry.getExecutor(dtpName);

        List<String> receivers = StrUtil.split(platform.getReceivers(), ',');
        String receivesStr = Joiner.on(", @").join(receivers);

        NotifyItem notifyItem = contextWrapper.getNotifyItem();
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
                executor.getRejectCount(),
                receivesStr,
                DateUtil.now(),
                notifyItem.getInterval()
        );
        return highlightAlarmContent(content, typeEnum);
    }

    public String buildNoticeContent(NotifyPlatform platform,
                                     String template,
                                     DtpMainProp oldProp,
                                     List<String> diffs) {
        String threadPoolName = oldProp.getDtpName();
        DtpExecutor dtpExecutor = DtpRegistry.getExecutor(threadPoolName);

        List<String> receivers = StrUtil.split(platform.getReceivers(), ',');
        String receivesStr = Joiner.on(", @").join(receivers);

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

        List<String> colorKeys = Lists.newArrayList();
        if (typeEnum == NotifyTypeEnum.REJECT) {
            colorKeys = REJECT_ALARM_KEYS;
        } else if (typeEnum == NotifyTypeEnum.CAPACITY) {
            colorKeys = CAPACITY_ALARM_KEYS;
        } else if (typeEnum == NotifyTypeEnum.LIVENESS) {
            colorKeys = LIVENESS_ALARM_KEYS;
        }

        colorKeys.addAll(COMMON_ALARM_KEYS);
        Pair<String, String> pair = getColors();
        for (String field : colorKeys) {
            content = content.replace(field, pair.getLeft());
        }
        for (String field : ALL_ALARM_KEYS) {
            content = content.replace(field, pair.getRight());
        }
        return content;
    }
}
