package io.lyh.dtp.handler;

import io.lyh.dtp.core.DtpExecutor;
import io.lyh.dtp.monitor.LogCollector;
import io.lyh.dtp.monitor.MicroMeterCollector;
import io.lyh.dtp.monitor.MetricsCollector;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ServiceLoader;

/**
 * CollectorHandler related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-29 18:06
 * @since 1.0.0
 **/
@Slf4j
public class CollectorHandler {

    private static final List<MetricsCollector> COLLECTORS = Lists.newArrayList();

    private static class CollectorHandlerHolder {
        private static final CollectorHandler INSTANCE = new CollectorHandler();
    }

    private CollectorHandler() {
        ServiceLoader<MetricsCollector> loader = ServiceLoader.load(MetricsCollector.class);
        for (MetricsCollector collector : loader) {
            COLLECTORS.add(collector);
        }

        MetricsCollector defaultCollector = new MicroMeterCollector();
        LogCollector logCollector = new LogCollector();
        COLLECTORS.add(defaultCollector);
        COLLECTORS.add(logCollector);
    }

    public static CollectorHandler getInstance() {
        return CollectorHandler.CollectorHandlerHolder.INSTANCE;
    }

    public void collect(DtpExecutor executor) {

        COLLECTORS.forEach(x -> {
            try {
                x.collect(executor);
            } catch (Exception e) {
                log.error("Dynamic collect failed, collector: {}", x.getClass().getSimpleName(), e);
            }
        });
    }
}
