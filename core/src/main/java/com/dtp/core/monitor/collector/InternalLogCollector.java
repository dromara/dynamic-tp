package com.dtp.core.monitor.collector;

import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.common.em.CollectorTypeEnum;
import com.dtp.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Redick01
 */
@Slf4j
public class InternalLogCollector extends AbstractCollector {

    @Override
    public void collect(ThreadPoolStats poolStats) {
        log.info("dynamic.tp metrics: {}", JsonUtil.toJson(poolStats));
    }

    @Override
    public String type() {
        return CollectorTypeEnum.INTERNAL_LOGGING.name().toLowerCase();
    }
}
