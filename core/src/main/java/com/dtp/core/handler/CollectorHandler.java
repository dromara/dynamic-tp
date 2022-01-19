package com.dtp.core.handler;

import com.dtp.core.DtpExecutor;
import com.dtp.core.monitor.collector.LogCollector;
import com.dtp.core.monitor.collector.MetricsCollector;
import com.dtp.core.monitor.collector.MicroMeterCollector;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ServiceLoader;

/**
 * CollectorHandler related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class CollectorHandler {

    private static final List<MetricsCollector> COLLECTORS = Lists.newArrayList();

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

    public void collect(DtpExecutor executor, String type) {
        for (MetricsCollector collector : COLLECTORS) {
            if (collector.support(type)) {
                collector.collect(executor);
                break;
            }
        }
    }

    public static CollectorHandler getInstance() {
        return CollectorHandlerHolder.INSTANCE;
    }

    private static class CollectorHandlerHolder {
        private static final CollectorHandler INSTANCE = new CollectorHandler();
    }
}
