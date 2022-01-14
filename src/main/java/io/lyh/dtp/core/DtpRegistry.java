package io.lyh.dtp.core;

import cn.hutool.core.collection.CollUtil;
import io.lyh.dtp.config.DtpProperties;
import io.lyh.dtp.common.em.NotifyTypeEnum;
import io.lyh.dtp.common.em.RejectedTypeEnum;
import io.lyh.dtp.common.ex.DtpException;
import io.lyh.dtp.support.DtpMainPropWrapper;
import io.lyh.dtp.config.ThreadPoolProperties;
import io.lyh.dtp.handler.NotifierHandler;
import io.lyh.dtp.notify.AlarmLimiter;
import io.lyh.dtp.notify.NotifyHelper;
import io.lyh.dtp.support.DtpCreator;
import io.lyh.dtp.support.VariableLinkedBlockingQueue;
import com.github.dadiyang.equator.Equator;
import com.github.dadiyang.equator.FieldInfo;
import com.github.dadiyang.equator.GetterBaseEquator;
import com.google.common.collect.Lists;
import io.lyh.dtp.common.constant.DynamicTpConst;
import io.lyh.dtp.common.em.QueueTypeEnum;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Core Registry, which keeps all registered Dynamic ThreadPoolExecutors.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpRegistry implements InitializingBean {

    private static final ExecutorService NOTIFY_EXECUTOR = DtpCreator.createWithTtl("dtp-notify");

    private static final Map<String, DtpExecutor> DTP_REGISTRY = new ConcurrentHashMap<>();

    private static final Equator EQUATOR = new GetterBaseEquator();

    private static DtpProperties dtpProperties;

    /**
     * Register the Dynamic ThreadPoolExecutors.
     * @param executor DtpExecutor instance.
     */
    public static void register(DtpExecutor executor) {
        log.info("DynamicTp register, executor: {}", DtpMainPropWrapper.of(executor));
        DTP_REGISTRY.put(executor.getThreadPoolName(), executor);
    }

    /**
     * Get Dynamic ThreadPoolExecutor by threadPoolName.
     * @param name threadPoolName
     * @return The managed DtpExecutor instance.
     */
    public static DtpExecutor getExecutor(String name) {
        val executor= DTP_REGISTRY.get(name);
        if (Objects.isNull(executor)) {
            log.error("Cannot find a specified DynamicTp, name: {}", name);
            throw new DtpException("Cannot find a specified DynamicTp, name: " + name);
        }
        return executor;
    }

    /**
     * Refresh while the listening configuration changes.
     * @param properties Main properties that maintain by the config center.
     */
    public static void refresh(DtpProperties properties) {
        if (Objects.isNull(properties) || CollUtil.isEmpty(properties.getExecutors())) {
            log.warn("DynamicTp refresh, empty threadPoolProperties.");
            return;
        }
        properties.getExecutors().forEach(x -> {
            if (StringUtils.isBlank(x.getThreadPoolName())) {
                log.warn("DynamicTp refresh, threadPoolName must not be empty.");
                return;
            }
            val dtpExecutor = DTP_REGISTRY.get(x.getThreadPoolName());
            if (Objects.isNull(dtpExecutor)) {
                log.warn("DynamicTp refresh, cannot find specified executor, name: {}.", x.getThreadPoolName());
                return;
            }
            refresh(dtpExecutor, x);
        });
    }

    public static void refresh(DtpExecutor executor, ThreadPoolProperties properties) {
        DtpMainPropWrapper oldProp = DtpMainPropWrapper.of(executor);
        doRefresh(executor, properties);
        DtpMainPropWrapper newProp = DtpMainPropWrapper.of(executor);
        if (oldProp.equals(newProp)) {
            log.warn("DynamicTp [{}] has no property changes.", executor.getThreadPoolName());
            return;
        }

        List<FieldInfo> diffFields = EQUATOR.getDiffFields(oldProp, newProp);
        List<String> diffKeys = diffFields.stream().map(FieldInfo::getFieldName).collect(Collectors.toList());
        DtpContext contextWrapper = DtpContext.builder()
                .dtpExecutor(executor)
                .platforms(dtpProperties.getPlatforms())
                .notifyItem(NotifyHelper.getNotifyItem(executor, NotifyTypeEnum.CHANGE))
                .build();
        DtpContextHolder.set(contextWrapper);
        NOTIFY_EXECUTOR.execute(() -> NotifierHandler.getInstance().sendNotice(oldProp, diffKeys));

        log.info("DynamicTp [{}] refresh end, changed keys: {}, corePoolSize: [{}], maxPoolSize: [{}], " +
                        "queueType: [{}], queueCapacity: [{}], keepAliveTime: [{}], rejectedType: [{}], " +
                        "allowsCoreThreadTimeOut: [{}]",
                executor.getThreadPoolName(),
                diffKeys,
                String.format(DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getCorePoolSize(), newProp.getCorePoolSize()),
                String.format(DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getMaxPoolSize(), newProp.getMaxPoolSize()),
                String.format(DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getQueueType(), newProp.getQueueType()),
                String.format(DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getQueueCapacity(), newProp.getQueueCapacity()),
                String.format("%ss => %ss", oldProp.getKeepAliveTime(), newProp.getKeepAliveTime()),
                String.format(DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE, RejectedTypeEnum.formatRejectName(oldProp.getRejectType()),
                        RejectedTypeEnum.formatRejectName(newProp.getRejectType())),
                String.format(DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE, oldProp.isAllowCoreThreadTimeOut(),
                        newProp.isAllowCoreThreadTimeOut()));
    }

    public static void doRefresh(DtpExecutor dtpExecutor, ThreadPoolProperties properties) {

        if (properties.getMaximumPoolSize() >= 0) {
            dtpExecutor.setMaximumPoolSize(properties.getMaximumPoolSize());
        }

        // jdk1.8 bug：setCorePoolSize未与maximumPoolSize比较
        if (properties.getCorePoolSize() > dtpExecutor.getMaximumPoolSize()) {
            throw new IllegalArgumentException();
        }

        if (properties.getCorePoolSize() >= 0) {
            dtpExecutor.setCorePoolSize(properties.getCorePoolSize());
        }

        if (properties.getKeepAliveTime() > 0 && properties.getUnit() != null) {
            dtpExecutor.setKeepAliveTime(properties.getKeepAliveTime(), properties.getUnit());
        }

        String originRejectedName = dtpExecutor.getRejectHandlerName();
        if (StringUtils.isNotBlank(properties.getRejectedHandlerType()) &&
                !originRejectedName.contains(properties.getRejectedHandlerType())) {
            dtpExecutor.setRejectedExecutionHandler(
                    RejectedTypeEnum.buildRejectedHandler(properties.getRejectedHandlerType()));
        }

        if (properties.getQueueCapacity() > 0 &&
                Objects.equals(properties.getQueueType(), QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE.getName())) {

            val blockingQueue = dtpExecutor.getQueue();
            if (blockingQueue instanceof VariableLinkedBlockingQueue) {
                ((VariableLinkedBlockingQueue)blockingQueue).setCapacity(properties.getQueueCapacity());
            } else {
                log.error("DynamicTp refresh, the blockingqueue capacity cannot be reset, dtpName: {}, queue {}",
                        dtpExecutor.getThreadPoolName(), dtpExecutor.getQueueName());
            }
        }
        dtpExecutor.allowCoreThreadTimeOut(properties.isAllowCoreThreadTimeOut());

        if (CollUtil.isNotEmpty(properties.getNotifyItems())) {
            NotifyHelper.fillNotifyItems(dtpProperties.getPlatforms(), properties.getNotifyItems());
            dtpExecutor.setNotifyItems(properties.getNotifyItems());
            properties.getNotifyItems().forEach(x -> AlarmLimiter.initAlarmLimiter(dtpExecutor.getThreadPoolName(), x));
        }
    }

    public static List<String> listAllDtpNames() {
        return Lists.newArrayList(DTP_REGISTRY.keySet());
    }

    @Autowired
    public void setDtpProperties(DtpProperties dtpProperties) {
        DtpRegistry.dtpProperties = dtpProperties;
    }

    @Override
    public void afterPropertiesSet() {

        if (CollectionUtils.isEmpty(dtpProperties.getExecutors())) {
            return;
        }
        dtpProperties.getExecutors().forEach(x -> {
            val executor = ThreadPoolBuilder.newBuilder()
                    .corePoolSize(x.getCorePoolSize())
                    .maximumPoolSize(x.getMaximumPoolSize())
                    .keepAliveTime(x.getKeepAliveTime())
                    .workQueue(x.getQueueType(), x.getQueueCapacity(), x.isFair())
                    .rejectedExecutionHandler(x.getRejectedHandlerType())
                    .threadFactory(x.getThreadNamePrefix())
                    .allowCoreThreadTimeOut(x.isAllowCoreThreadTimeOut())
                    .threadPoolName(x.getThreadPoolName())
                    .notifyItems(x.getNotifyItems())
                    .buildDynamic();
            register(executor);
        });

        DTP_REGISTRY.forEach((k, v) -> {
            NotifyHelper.fillNotifyItems(dtpProperties.getPlatforms(), v.getNotifyItems());
            v.getNotifyItems().forEach(x -> {
                // change notify not need to register alarm limiting.
                if (x.getInterval() == null) {
                    return;
                }
                AlarmLimiter.initAlarmLimiter(k, x);
            });
        });
    }
}
