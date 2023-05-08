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

package org.dromara.dynamictp.example.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static org.dromara.dynamictp.example.mq.RocketMqListener.GROUP;
import static org.dromara.dynamictp.example.mq.RocketMqListener.TOPIC;

/**
 * TestRocketMqListener related
 *
 * @author yanhom
 * @since 1.0.9
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = GROUP, topic = TOPIC)
public class RocketMqListener implements RocketMQListener<MessageExt> {

    public static final String GROUP = "group";
    public static final String TOPIC = "topic";

    @Override
    public void onMessage(MessageExt message) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("message: {}", message);
    }
}
