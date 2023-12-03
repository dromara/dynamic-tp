package org.dromara.dynamictp.example.collector;

import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.util.JsonUtil;
import org.dromara.dynamictp.core.monitor.collector.AbstractCollector;

/**
 * EsCollector related
 *
 * @author yanhom
 * @since 1.1.0
 */
public class EsCollector extends AbstractCollector {

    private final EsClient esClient;

    public EsCollector(EsClient esClient) {
        this.esClient = esClient;
    }

    @Override
    public void collect(ThreadPoolStats poolStats) {
        esClient.save(JsonUtil.toJson(poolStats));
    }

    @Override
    public String type() {
        return "es";
    }
}
