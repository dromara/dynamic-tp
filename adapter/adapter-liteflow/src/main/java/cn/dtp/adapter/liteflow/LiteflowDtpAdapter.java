package cn.dtp.adapter.liteflow;

import cn.hutool.core.collection.CollUtil;
import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.ReflectionUtil;
import com.yomahub.liteflow.thread.ExecutorHelper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * LiteflowDtpAdapter related
 *
 * @author yanhom
 * @since 1.1.0
 */
@SuppressWarnings("all")
@Slf4j
public class LiteflowDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "liteflowTp";

    private static final String EXECUTOR_FIELD_NAME = "executorServiceMap";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getLiteflowTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();

        ExecutorHelper executorHelper = ExecutorHelper.loadInstance();
        val executorMap = (Map<String, ExecutorService>) ReflectionUtil.getFieldValue(ExecutorHelper.class,
                EXECUTOR_FIELD_NAME, executorHelper);
        if (CollUtil.isEmpty(executorMap)) {
            log.warn("Empty executorServiceMap.");
            return;
        }
        executorMap.forEach((k, v) -> {
            val executorWrapper = new ExecutorWrapper(k, v);
            initNotifyItems(k, executorWrapper);
            executors.put(k, executorWrapper);
        });
        log.info("DynamicTp adapter, liteflow executors init end, executors: {}", executors);
    }
}
