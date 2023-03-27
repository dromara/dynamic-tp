package com.dtp.adapter.rocketmq;

import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.ReflectionUtil;
import com.dtp.core.support.ExecutorWrapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.MapUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService;
import org.apache.rocketmq.client.impl.consumer.ConsumeMessageOrderlyService;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * RocketMqDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.6
 */
@SuppressWarnings("all")
@Slf4j
public class RocketMqDtpAdapter extends AbstractDtpAdapter {

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
        adaptProducerExecutors();

        log.info("DynamicTp adapter, rocketMq consumer and producer executors init end, executors: {}", executors);
    }

    public void adaptConsumerExecutors() {

        val beans = ApplicationContextHolder.getBeansOfType(DefaultRocketMQListenerContainer.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type DefaultRocketMQListenerContainer.");
            return;
        }
        beans.forEach((k, v) -> {
            DefaultRocketMQListenerContainer container = (DefaultRocketMQListenerContainer) v;
            DefaultMQPushConsumer consumer = container.getConsumer();
            val pushConsumer = (DefaultMQPushConsumerImpl) ReflectionUtil.getFieldValue(DefaultMQPushConsumer.class,
                    "defaultMQPushConsumerImpl", consumer);
            if (Objects.isNull(pushConsumer)) {
                return;
            }

            String cusKey = container.getConsumerGroup() + "#" + container.getTopic();
            ThreadPoolExecutor executor = null;
            val consumeMessageService = pushConsumer.getConsumeMessageService();
            if (consumeMessageService instanceof ConsumeMessageConcurrentlyService) {
                executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(ConsumeMessageConcurrentlyService.class,
                        CONSUME_EXECUTOR_FIELD_NAME, consumeMessageService);
            } else if (consumeMessageService instanceof ConsumeMessageOrderlyService) {
                executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(ConsumeMessageOrderlyService.class,
                        CONSUME_EXECUTOR_FIELD_NAME, consumeMessageService);
            }
            if (Objects.nonNull(executor)) {
                val executorWrapper = new ExecutorWrapper(cusKey, executor);
                initNotifyItems(cusKey, executorWrapper);
                executors.put(cusKey, executorWrapper);
            }
        });
    }

    public void adaptProducerExecutors() {

        val beans = ApplicationContextHolder.getBeansOfType(DefaultMQProducer.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type TransactionMQProducer.");
            return;
        }
        beans.forEach((k, v) -> {
            DefaultMQProducer defaultMQProducer = (DefaultMQProducer) v;
            val producer = (DefaultMQProducerImpl) ReflectionUtil.getFieldValue(DefaultMQProducer.class,
                    "defaultMQProducerImpl", defaultMQProducer);
            if (Objects.isNull(producer)) {
                return;
            }

            String proKey = defaultMQProducer.getProducerGroup() + "#" + defaultMQProducer.getCreateTopicKey();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) producer.getAsyncSenderExecutor();

            if (Objects.nonNull(executor)) {
                val executorWrapper = new ExecutorWrapper(proKey, executor);
                initNotifyItems(proKey, executorWrapper);
                executors.put(proKey, executorWrapper);
            }
        });
    }
}
