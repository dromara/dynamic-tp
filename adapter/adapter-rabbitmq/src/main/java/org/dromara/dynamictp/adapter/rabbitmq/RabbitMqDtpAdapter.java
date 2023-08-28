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

package org.dromara.dynamictp.adapter.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.MapUtils;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.spring.ApplicationContextHolder;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.springframework.amqp.rabbit.connection.AbstractConnectionFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

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

    private static final String CONSUME_EXECUTOR_FIELD_NAME = "executorService";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getRabbitmqTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();

        val beans = ApplicationContextHolder.getBeansOfType(AbstractConnectionFactory.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type AbstractConnectionFactory.");
            return;
        }
        beans.forEach((k, v) -> {
            AbstractConnectionFactory abstractConnectionFactory = (AbstractConnectionFactory) v;
            ExecutorService executor = (ExecutorService) ReflectionUtil.getFieldValue(
                    AbstractConnectionFactory.class, CONSUME_EXECUTOR_FIELD_NAME, abstractConnectionFactory);

            if (Objects.nonNull(executor)) {
                String key = genTpName(k);
                val executorWrapper = new ExecutorWrapper(key, executor);
                initNotifyItems(key, executorWrapper);
                executors.put(key, executorWrapper);
                if (executor instanceof ThreadPoolExecutor) {
                    ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(executorWrapper);
                    try {
                        ReflectionUtil.setFieldValue(AbstractConnectionFactory.class, CONSUME_EXECUTOR_FIELD_NAME, abstractConnectionFactory, proxy);
                    } catch (IllegalAccessException e) {
                        log.error("RabbiMq executor proxy exception", e);
                    }
                }
            }
        });
        log.info("DynamicTp adapter, rabbitmq executors init end, executors: {}", executors);
    }

    private String genTpName(String beanName) {
        return beanName + "Tp";
    }
}
