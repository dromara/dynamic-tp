package io.lyh.dtp.monitor.collector;

import cn.hutool.json.JSONUtil;
import io.lyh.dtp.common.em.CollectorTypeEnum;
import io.lyh.dtp.core.DtpExecutor;
import io.lyh.dtp.monitor.MetricsHelper;
import io.lyh.dtp.util.LogUtil;

/**
 * LogCollector related
 *
 * @author: yanhom
 * @since 1.0.0
 */
public class LogCollector extends AbstractCollector {

    @Override
    public void collect(DtpExecutor executor) {
        String metrics = JSONUtil.toJsonStr(MetricsHelper.getMetrics(executor));
        LogUtil.MONITOR_LOGGER.info("{}", metrics);
    }

    @Override
    public String type() {
        return CollectorTypeEnum.LOGGING.name();
    }
}
