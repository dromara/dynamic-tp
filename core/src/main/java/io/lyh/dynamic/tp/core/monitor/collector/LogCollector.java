package io.lyh.dynamic.tp.core.monitor.collector;

import cn.hutool.json.JSONUtil;
import io.lyh.dynamic.tp.common.em.CollectorTypeEnum;
import io.lyh.dynamic.tp.common.util.LogUtil;
import io.lyh.dynamic.tp.core.DtpExecutor;
import io.lyh.dynamic.tp.core.helper.MetricsHelper;

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
