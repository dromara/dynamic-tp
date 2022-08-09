package com.dtp.core.monitor.collector;

import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.common.em.CollectorTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Redick01
 */
@Slf4j
public class InternalLogCollector extends AbstractCollector {

    @Override
    public void collect(ThreadPoolStats poolStats) {
        log.info("dynamic.tp metrics: {}", poolStats);
    }

    @Override
    public String type() {
        return CollectorTypeEnum.INTERNAL_LOGGING.name();
    }
}
