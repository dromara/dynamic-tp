package io.lyh.dtp.monitor;

import cn.hutool.json.JSONUtil;
import io.lyh.dtp.core.DtpExecutor;
import io.lyh.dtp.util.LogUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * LogCollector related
 *
 * @author: yanhom1314@gmail.com
 * @date 2022-01-06 上午12:21
 */
@Slf4j
public class LogCollector implements MetricsCollector {

    @Override
    public void collect(DtpExecutor executor) {
        String metrics = JSONUtil.toJsonStr(MetricsHelper.getMetrics(executor));
        LogUtil.MONITOR_LOGGER.info("{}", metrics);
    }
}
