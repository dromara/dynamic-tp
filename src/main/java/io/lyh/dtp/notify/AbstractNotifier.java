package io.lyh.dtp.notify;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Joiner;
import io.lyh.dtp.common.constant.DynamicTpConst;
import io.lyh.dtp.common.em.NotifyTypeEnum;
import io.lyh.dtp.common.em.RejectedTypeEnum;
import io.lyh.dtp.core.DtpContextHolder;
import io.lyh.dtp.core.DtpContext;
import io.lyh.dtp.core.DtpExecutor;
import io.lyh.dtp.core.DtpRegistry;
import io.lyh.dtp.support.ApplicationContextHolder;
import io.lyh.dtp.support.DtpMainPropWrapper;
import io.lyh.dtp.support.Instance;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AbstractNotifier related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-31 17:09
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
        Environment environment = ApplicationContextHolder.getInstance().getEnvironment();
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length < 1) {
            profiles = environment.getDefaultProfiles();
        }

        String appName = environment.getProperty("spring.application.name");
        appName = StringUtils.isNoneBlank(appName) ? appName : "application";

        String portStr = environment.getProperty("server.port");
        int port = StringUtils.isNotBlank(portStr) ? Integer.parseInt(portStr) : 0;
        String address = InetAddress.getLocalHost().getHostAddress();
        instance = new Instance(address, port, appName, profiles[0]);
    }

    public String buildAlarmContent(NotifyPlatform platform, NotifyTypeEnum typeEnum, String template) {
        DtpContext contextWrapper = DtpContextHolder.get();
        DtpExecutor executor = DtpRegistry.getExecutor(contextWrapper.getDtpExecutor().getThreadPoolName());

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
                RejectedTypeEnum.formatRejectName(executor.getRejectHandlerName()),
                executor.getRejectCount(),
                receivesStr,
                DateUtil.now(),
                notifyItem.getInterval()
        );
        return highlightAlarmContent(content, typeEnum);
    }

    public String buildNoticeContent(NotifyPlatform platform,
                                     String template,
                                     DtpMainPropWrapper oldPropWrapper,
                                     List<String> diffs) {
        String threadPoolName = oldPropWrapper.getDtpName();
        DtpExecutor dtpExecutor = DtpRegistry.getExecutor(threadPoolName);

        List<String> receivers = StrUtil.split(platform.getReceivers(), ',');
        String receivesStr = Joiner.on(", @").join(receivers);

        String content = String.format(
                template,
                getInstance().getServiceName(),
                getInstance().getIp() + ":" + getInstance().getPort(),
                getInstance().getEnv(),
                threadPoolName,
                oldPropWrapper.getCorePoolSize(),
                dtpExecutor.getCorePoolSize(),
                oldPropWrapper.getMaxPoolSize(),
                dtpExecutor.getMaximumPoolSize(),
                oldPropWrapper.isAllowCoreThreadTimeOut(),
                dtpExecutor.allowsCoreThreadTimeOut(),
                oldPropWrapper.getKeepAliveTime(),
                dtpExecutor.getKeepAliveTime(TimeUnit.SECONDS),
                dtpExecutor.getQueueName(),
                oldPropWrapper.getQueueCapacity(),
                dtpExecutor.getQueueCapacity(),
                RejectedTypeEnum.formatRejectName(oldPropWrapper.getRejectType()),
                RejectedTypeEnum.formatRejectName(dtpExecutor.getRejectHandlerName()),
                receivesStr,
                DateTime.now()
        );
        return highlightNotifyContent(content, diffs);
    }

    /**
     * Implement by subclass, get color config.
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
        for (Field field : DtpMainPropWrapper.getMainProps()) {
            content = content.replace(field.getName(), pair.getRight());
        }
        return content;
    }

    private String highlightAlarmContent(String content, NotifyTypeEnum typeEnum) {
        if (StringUtils.isBlank(content)) {
            return content;
        }

        List<String> colorKeys = Collections.emptyList();
        if (typeEnum == NotifyTypeEnum.REJECT) {
            colorKeys = DynamicTpConst.REJECT_ALARM_KEYS;
        } else if (typeEnum == NotifyTypeEnum.CAPACITY) {
            colorKeys = DynamicTpConst.CAPACITY_ALARM_KEYS;
        } else if (typeEnum == NotifyTypeEnum.LIVENESS) {
            colorKeys = DynamicTpConst.LIVENESS_ALARM_KEYS;
        }

        Pair<String, String> pair = getColors();
        for (String field : colorKeys) {
            content = content.replace(field, pair.getLeft());
        }
        for (String field : DynamicTpConst.ALL_ALARM_KEYS) {
            content = content.replace(field, pair.getRight());
        }
        return content;
    }
}
