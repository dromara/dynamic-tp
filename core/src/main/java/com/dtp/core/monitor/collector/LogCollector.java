package com.dtp.core.monitor.collector;

import cn.hutool.json.JSONUtil;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.common.em.CollectorTypeEnum;
import com.dtp.common.util.LogUtil;

/**
 * LogCollector related
 *
 * @author: yanhom
 * @since 1.0.0
 */
public class LogCollector extends AbstractCollector {

    @Override
    public void collect(ThreadPoolStats threadPoolStats) {
        String metrics = JSONUtil.toJsonStr(threadPoolStats);
        LogUtil.MONITOR_LOGGER.info("{}", metrics);
    }

    @Override
    public String type() {
        return CollectorTypeEnum.LOGGING.name();
    }
}
