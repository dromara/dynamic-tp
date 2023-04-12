package com.dtp.adapter.rabbitmq;

import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.ReflectionUtil;
import com.dtp.core.support.ExecutorWrapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.MapUtils;
import org.springframework.amqp.rabbit.connection.AbstractConnectionFactory;

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
            ThreadPoolExecutor executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(
                    AbstractConnectionFactory.class, CONSUME_EXECUTOR_FIELD_NAME, abstractConnectionFactory);

            if (Objects.nonNull(executor)) {
                String key = genTpName(k);
                val executorWrapper = new ExecutorWrapper(key, executor);
                initNotifyItems(key, executorWrapper);
                executors.put(key, executorWrapper);
            }
        });
        log.info("DynamicTp adapter, rabbitmq executors init end, executors: {}", executors);
    }

    private String genTpName(String beanName) {
        return beanName + "Tp";
    }
}
