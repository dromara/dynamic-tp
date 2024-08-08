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
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
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

    private static final String TP_PREFIX = "rabbitMqTp";

    private static final String CONSUME_EXECUTOR_FIELD = "executorService";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(dtpProperties.getRabbitmqTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }

    @Override
    protected void initialize() {
        super.initialize();

        val beans = ContextManagerHelper.getBeansOfType(AbstractConnectionFactory.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type AbstractConnectionFactory.");
            return;
        }
        beans.forEach((k, v) -> {
            AbstractConnectionFactory connFactory = (AbstractConnectionFactory) v;
            ExecutorService executor = (ExecutorService) ReflectionUtil.getFieldValue(
                    AbstractConnectionFactory.class, CONSUME_EXECUTOR_FIELD, connFactory);
            if (Objects.nonNull(executor) && executor instanceof ThreadPoolExecutor) {
                String tpName = genTpName(k);
                enhanceOriginExecutor(tpName, (ThreadPoolExecutor) executor, CONSUME_EXECUTOR_FIELD, connFactory);
            }
        });
    }

    private String genTpName(String beanName) {
        return TP_PREFIX + "#" + beanName;
    }
}
