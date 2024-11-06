/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.adapter.rocketmq;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService;
import org.apache.rocketmq.client.impl.consumer.ConsumeMessageOrderlyService;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.proxy.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.jvmti.JVMTI;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * RocketMqDtpAdapter related
 *
 * @author yanhom
 * @author dragon-zhang
 * @since 1.0.6
 */
@SuppressWarnings("all")
@Slf4j
public class RocketMqDtpAdapter extends AbstractDtpAdapter {

    private static final String TP_PREFIX = "rocketMqTp";
    private static final String CONSUME_EXECUTOR_FIELD = "consumeExecutor";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(dtpProperties.getRocketMqTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }

    @Override
    protected void initialize() {
        super.initialize();
        adaptConsumerExecutors();
        adaptProducerExecutors();
    }

    public void adaptConsumerExecutors() {
        val beans = JVMTI.getInstances(DefaultMQPushConsumer.class);
        if (CollectionUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type DefaultMQPushConsumer.");
            return;
        }
        for (DefaultMQPushConsumer consumer : beans) {
            val pushConsumer = (DefaultMQPushConsumerImpl) ReflectionUtil.getFieldValue(DefaultMQPushConsumer.class,
                    "defaultMQPushConsumerImpl", consumer);
            if (Objects.isNull(pushConsumer)) {
                continue;
            }
            val consumeMessageService = pushConsumer.getConsumeMessageService();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(consumeMessageService.getClass(),
                    CONSUME_EXECUTOR_FIELD, consumeMessageService);
            if (Objects.nonNull(executor)) {
                String tpName = consumer.getConsumerGroup();
                if (consumeMessageService instanceof ConsumeMessageConcurrentlyService) {
                    tpName = TP_PREFIX + "#consumer#concurrently#" + tpName;
                } else if (consumeMessageService instanceof ConsumeMessageOrderlyService) {
                    tpName = TP_PREFIX + "#consumer#orderly#" + tpName;
                }
                enhanceOriginExecutor(tpName, executor, CONSUME_EXECUTOR_FIELD, consumeMessageService);
            }
        }
    }

    public void adaptProducerExecutors() {
        val beans = JVMTI.getInstances(DefaultMQProducer.class);
        if (CollectionUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type DefaultMQProducer.");
            return;
        }
        for (DefaultMQProducer defaultMQProducer : beans) {
            val method = ReflectionUtil.findMethod(DefaultMQProducerImpl.class, "getAsyncSenderExecutor");
            if (Objects.isNull(method)) {
                continue;
            }
            val producer = (DefaultMQProducerImpl) ReflectionUtil.getFieldValue(DefaultMQProducer.class,
                    "defaultMQProducerImpl", defaultMQProducer);
            if (Objects.isNull(producer)) {
                continue;
            }
            ThreadPoolExecutor executor = (ThreadPoolExecutor) producer.getAsyncSenderExecutor();
            if (Objects.nonNull(executor)) {
                ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(executor);
                producer.setAsyncSenderExecutor(proxy);
                String proKey = TP_PREFIX + "#producer#" + defaultMQProducer.getProducerGroup();
                putAndFinalize(proKey, executor, proxy);
            }
        }
    }
}
