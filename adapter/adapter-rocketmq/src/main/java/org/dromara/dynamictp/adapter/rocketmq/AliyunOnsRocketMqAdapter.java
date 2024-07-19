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

import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.impl.rocketmq.ConsumerImpl;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.impl.consumer.ConsumeMessageOrderlyService;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.MapUtils;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Aliyun business version rocketmq adapter.
 * @author Redick01
 */
@Slf4j
public class AliyunOnsRocketMqAdapter extends AbstractDtpAdapter {

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
    }

    private void adaptConsumerExecutors() {
        // get consumer beans
        val beans = ContextManagerHelper.getBeansOfType(Consumer.class);
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

        val consumeMessageService = impl.getConsumeMessageService();
        String tpName = defaultMqPushConsumer.getConsumerGroup();
        if (consumeMessageService instanceof ConsumeMessageConcurrentlyService) {
            tpName = TP_PREFIX + "#consumer#concurrently#" + tpName;
        } else if (consumeMessageService instanceof ConsumeMessageOrderlyService) {
            tpName = TP_PREFIX + "#consumer#orderly#" + tpName;
        }
        ThreadPoolExecutor executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(CONSUME_EXECUTOR_FIELD, consumeMessageService);
        if (Objects.nonNull(executor)) {
            enhanceOriginExecutor(tpName, executor, CONSUME_EXECUTOR_FIELD, consumeMessageService);
        }
    }
}
