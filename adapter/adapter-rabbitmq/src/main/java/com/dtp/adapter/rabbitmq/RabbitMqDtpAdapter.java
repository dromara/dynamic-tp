package com.dtp.adapter.rabbitmq;

import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.properties.DtpProperties;
import lombok.extern.slf4j.Slf4j;

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

    }
}
