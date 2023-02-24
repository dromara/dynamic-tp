package com.dtp.adapter.rabbitmq;

import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.ReflectionUtil;
import com.dtp.core.support.ExecutorWrapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.MapUtils;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.AbstractConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.util.Objects;
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
        refresh(NAME, dtpProperties.getRocketMqTp(), dtpProperties.getPlatforms());
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
            ThreadPoolExecutor executor = null;
            executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(AbstractConnectionFactory.class,
                    CONSUME_EXECUTOR_FIELD_NAME, abstractConnectionFactory);

            if (Objects.nonNull(executor)) {
                val executorWrapper = new ExecutorWrapper(NAME, executor);
                initNotifyItems(NAME, executorWrapper);
                executors.put(NAME, executorWrapper);
            }
        });

        log.info("DynamicTp adapter, rabbitmq executors init end, executors: {}", executors);
    }
}
