package org.dromara.dynamictp.adapter.okhttp3;

import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.ApplicationContextHolder;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.common.properties.DtpProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.OkHttpClient;
import org.apache.commons.collections4.MapUtils;

/**
 * Okhttp3DtpAdapter related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
public class Okhttp3DtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "okhttp3Tp";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getOkhttp3Tp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();
        val beans = ApplicationContextHolder.getBeansOfType(OkHttpClient.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type OkHttpClient.");
            return;
        }
        beans.forEach((k, v) -> {
            val executor = v.dispatcher().executorService();
            String key = genTpName(k);
            val executorWrapper = new ExecutorWrapper(key, executor);
            initNotifyItems(key, executorWrapper);
            executors.put(key, executorWrapper);
        });
        log.info("DynamicTp adapter, okhttp3 executors init end, executors: {}", executors);
    }

    private String genTpName(String clientName) {
        return clientName + "Tp";
    }
}
