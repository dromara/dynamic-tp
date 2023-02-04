package com.dtp.core.monitor.collector;

import cn.hutool.json.JSONUtil;
import com.dtp.common.entity.ThreadPoolStats;
import com.dtp.common.em.CollectorTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Redick01
 */
@Slf4j
public class InternalLogCollector extends AbstractCollector {

    @Override
    public void collect(ThreadPoolStats poolStats) {
        log.info("dynamic.tp metrics: {}", JSONUtil.toJsonStr(poolStats));
    }

    @Override
    public String type() {
        return CollectorTypeEnum.INTERNAL_LOGGING.name().toLowerCase();
    }
}
