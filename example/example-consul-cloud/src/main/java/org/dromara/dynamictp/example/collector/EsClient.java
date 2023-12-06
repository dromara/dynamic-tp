package org.dromara.dynamictp.example.collector;

import lombok.extern.slf4j.Slf4j;

/**
 * EsClient related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
public class EsClient {

    public void save(String json) {
        log.info("save to es, json: {}", json);
    }
}
