package com.dtp.adapter.rocketmq;

import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.impl.rocketmq.ConsumerImpl;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.impl.consumer.ConsumeMessageOrderlyService;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.ReflectionUtil;
import com.dtp.core.support.ExecutorWrapper;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.MapUtils;

/**
 * Aliyun business version rocketmq adapter.
 * @author Redick01
 */
@Slf4j
public class AliyunOnsRocketMqAdapter extends AbstractDtpAdapter {

    private static final String NAME = "rocketMqTp";

    private static final String CONSUME_EXECUTOR_FIELD_NAME = "consumeExecutor";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getRocketMqTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();
        adaptConsumerExecutors();

        log.info("DynamicTp adapter, Aliyun business version RocketMQ consumer executors init end"
                + ", executors: {}", executors);
    }

    private void adaptConsumerExecutors() {
        // get consumer beans
        val beans = ApplicationContextHolder.getBeansOfType(Consumer.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type Consumer.");
            return;
        }
        beans.forEach(this::accept);
    }

    private void accept(String k, Consumer v) {
        val consumer = (ConsumerImpl) v;
        val defaultMqPushConsumer = (DefaultMQPushConsumer) ReflectionUtil.getFieldValue(
                ConsumerImpl.class, "defaultMQPushConsumer", consumer);
        if (Objects.isNull(defaultMqPushConsumer)) {
            return;
        }
        val impl = (DefaultMQPushConsumerImpl) ReflectionUtil.getFieldValue(
                DefaultMQPushConsumer.class, "defaultMQPushConsumerImpl", defaultMqPushConsumer);
        if (Objects.isNull(impl)) {
            return;
        }
        // consumer bean name replace topic name
        String cusKey = defaultMqPushConsumer.getConsumerGroup() + "#" + k;
        ThreadPoolExecutor executor = null;
        val consumeMessageService = impl.getConsumeMessageService();
        if (consumeMessageService instanceof ConsumeMessageConcurrentlyService) {
            executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(
                    ConsumeMessageConcurrentlyService.class,
                    CONSUME_EXECUTOR_FIELD_NAME, consumeMessageService);
        } else if (consumeMessageService instanceof ConsumeMessageOrderlyService) {
            executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(
                    ConsumeMessageOrderlyService.class,
                    CONSUME_EXECUTOR_FIELD_NAME, consumeMessageService);
        }
        if (Objects.nonNull(executor)) {
            val executorWrapper = new ExecutorWrapper(cusKey, executor);
            initNotifyItems(cusKey, executorWrapper);
            executors.put(cusKey, executorWrapper);
        }
    }
}
