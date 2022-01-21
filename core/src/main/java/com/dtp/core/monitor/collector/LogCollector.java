package com.dtp.core.monitor.collector;

import cn.hutool.json.JSONUtil;
import com.dtp.common.em.CollectorTypeEnum;
import com.dtp.common.util.LogUtil;
import com.dtp.core.DtpExecutor;
import com.dtp.core.helper.MetricsHelper;

/**
 * LogCollector related
 *
 * @author: yanhom
 * @since 1.0.0
 */
public class LogCollector extends AbstractCollector {

    @Override
    public void collect(DtpExecutor executor) {
        String metrics = JSONUtil.toJsonStr(MetricsHelper.getPoolStats(executor));
        LogUtil.MONITOR_LOGGER.info("{}", metrics);
    }

    @Override
    public String type() {
        return CollectorTypeEnum.LOGGING.name();
    }
}
