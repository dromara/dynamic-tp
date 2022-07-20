package com.dtp.core.monitor.collector;

import cn.hutool.json.JSONUtil;
import com.dtp.common.dto.ThreadPoolStats;
import com.dtp.common.em.CollectorTypeEnum;
import com.dtp.logging.LogHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * LogCollector related
 *
 * @author: yanhom
 * @since 1.0.0
 */
@Slf4j
public class LogCollector extends AbstractCollector {

    @Override
    public void collect(ThreadPoolStats threadPoolStats) {
        String metrics = JSONUtil.toJsonStr(threadPoolStats);
        if (LogHelper.getMonitorLogger() == null) {
            log.error("Cannot find monitor logger...");
            return;
        }
        LogHelper.getMonitorLogger().info("{}", metrics);
    }

    @Override
    public String type() {
        return CollectorTypeEnum.LOGGING.name();
    }
}
