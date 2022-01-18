package com.dtp.core.monitor.collector;

/**
 * AbstractCollector related
 *
 * @author: linyanhong@ihuman.com
 * @since 1.0.0
 **/
public abstract class AbstractCollector implements MetricsCollector {

    @Override
    public boolean support(String type) {
        return this.type().equalsIgnoreCase(type);
    }
}
