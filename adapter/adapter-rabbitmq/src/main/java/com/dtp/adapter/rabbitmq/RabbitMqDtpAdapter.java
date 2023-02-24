package com.dtp.adapter.rabbitmq;

import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.properties.DtpProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.MapUtils;
import org.springframework.amqp.rabbit.listener.DirectReplyToMessageListenerContainer;

/**
 * RabbitMqDtpAdapter related
 *
 * @author fabian4
 * @since 1.0.6
 */
@Slf4j
@SuppressWarnings("all")
public class RabbitMqDtpAdapter extends AbstractDtpAdapter {
    private static final String NAME = "rabbitMqTp";

    private static final String CONSUME_EXECUTOR_FIELD_NAME = "consumeExecutor";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getRocketMqTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();

        adaptCustomerExecutors();
//        adaptProducerExecutors();

        log.info("DynamicTp adapter, rabbitmq consumer and producer executors init end, executors: {}", executors);
    }

    public void adaptCustomerExecutors() {

        val beans = ApplicationContextHolder.getBeansOfType(DirectReplyToMessageListenerContainer.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type DefaultRocketMQListenerContainer.");
            return;
        }
        beans.forEach((k, v) -> {
//            DefaultRocketMQListenerContainer container = (DefaultRocketMQListenerContainer) v;
//            DefaultMQPushConsumer consumer = container.getConsumer();
//            val pushConsumer = (DefaultMQPushConsumerImpl) ReflectionUtil.getFieldValue(DefaultMQPushConsumer.class,
//                    "defaultMQPushConsumerImpl", consumer);
//            if (Objects.isNull(pushConsumer)) {
//                return;
//            }
//
//            String cusKey = container.getConsumerGroup() + "#" + container.getTopic();
//            ThreadPoolExecutor executor = null;
//            val consumeMessageService = pushConsumer.getConsumeMessageService();
//            if (consumeMessageService instanceof ConsumeMessageConcurrentlyService) {
//                executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(ConsumeMessageConcurrentlyService.class,
//                        CONSUME_EXECUTOR_FIELD_NAME, consumeMessageService);
//            } else if (consumeMessageService instanceof ConsumeMessageOrderlyService) {
//                executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(ConsumeMessageOrderlyService.class,
//                        CONSUME_EXECUTOR_FIELD_NAME, consumeMessageService);
//            }
//            if (Objects.nonNull(executor)) {
//                val executorWrapper = new ExecutorWrapper(cusKey, executor);
//                initNotifyItems(cusKey, executorWrapper);
//                executors.put(cusKey, executorWrapper);
//            }
        });
    }
}
