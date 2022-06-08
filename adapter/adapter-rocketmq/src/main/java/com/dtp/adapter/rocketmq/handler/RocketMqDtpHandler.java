package com.dtp.adapter.rocketmq.handler;

import cn.hutool.core.collection.CollUtil;
import com.dtp.adapter.common.AbstractDtpHandler;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.util.ReflectionUtil;
import com.dtp.common.util.StreamUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService;
import org.apache.rocketmq.client.impl.consumer.ConsumeMessageOrderlyService;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * RocketMqDtpHandler related
 *
 * @author yanhom
 * @since 1.0.6
 */
@SuppressWarnings("all")
@Slf4j
public class RocketMqDtpHandler extends AbstractDtpHandler {

    private static final String NAME = "rocketMqTp";

    private static final String CONSUME_EXECUTOR_FIELD_NAME = "consumeExecutor";

    private static final Map<String, ExecutorWrapper> ROCKETMQ_EXECUTORS = Maps.newHashMap();

    @Override
    public void refresh(DtpProperties dtpProperties) {
        val rocketMqTpList = dtpProperties.getRocketMqTp();
        val executors = getExecutorWrappers();
        if (CollUtil.isEmpty(rocketMqTpList) || CollUtil.isEmpty(executors)) {
            return;
        }

        val tmpMap = StreamUtil.toMap(rocketMqTpList, SimpleTpProperties::getThreadPoolName);
        executors.forEach((k ,v) -> updateBase(NAME, tmpMap.get(k), v));
    }

    @Override
    public Map<String, ExecutorWrapper> getExecutorWrappers() {

        if (CollUtil.isNotEmpty(ROCKETMQ_EXECUTORS)) {
            return ROCKETMQ_EXECUTORS;
        }
        val beans = ApplicationContextHolder.getBeansOfType(DefaultRocketMQListenerContainer.class);
        if (CollUtil.isEmpty(beans)) {
            return Collections.emptyMap();
        }
        beans.forEach((k, v) -> {
            DefaultRocketMQListenerContainer container = (DefaultRocketMQListenerContainer) v;
            DefaultMQPushConsumer consumer = container.getConsumer();
            val pushConsumer = (DefaultMQPushConsumerImpl) ReflectionUtil.getField(DefaultMQPushConsumer.class,
                    "defaultMQPushConsumerImpl", consumer);
            val consumeMessageService = pushConsumer.getConsumeMessageService();
            if (Objects.isNull(pushConsumer)) {
                return;
            }

            String key = container.getConsumerGroup() + "#" + container.getTopic();
            ThreadPoolExecutor executor = null;
            if (consumeMessageService instanceof ConsumeMessageConcurrentlyService) {
                executor = (ThreadPoolExecutor) ReflectionUtil.getField(ConsumeMessageConcurrentlyService.class,
                        CONSUME_EXECUTOR_FIELD_NAME, consumeMessageService);
            } else if (consumeMessageService instanceof ConsumeMessageOrderlyService) {
                executor = (ThreadPoolExecutor) ReflectionUtil.getField(ConsumeMessageOrderlyService.class,
                        CONSUME_EXECUTOR_FIELD_NAME, consumeMessageService);
            }
            if (Objects.nonNull(executor)) {
                val executorWrapper = new ExecutorWrapper(k, executor);
                ROCKETMQ_EXECUTORS.put(key, executorWrapper);
            }
        });
        log.info("DynamicTp adapter, rocketMq consumer executors init end, executors: {}", ROCKETMQ_EXECUTORS);
        return ROCKETMQ_EXECUTORS;
    }
}
