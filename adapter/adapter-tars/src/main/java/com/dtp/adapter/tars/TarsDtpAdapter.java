package com.dtp.adapter.tars;

import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.ReflectionUtil;
import com.qq.tars.client.Communicator;
import com.qq.tars.client.CommunicatorFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.MapUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * TarsDtpAdapter related
 *
 * @author yanhom
 * @since 1.1.0
 */
@SuppressWarnings("unchecked")
@Slf4j
public class TarsDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "tarsTp";

    private static final String COMMUNICATORS_FIELD = "CommunicatorMap";

    private static final String THREAD_POOL_FIELD = "threadPoolExecutor";

    private static final String COMMUNICATOR_ID_FIELD = "id";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getTarsTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();

        CommunicatorFactory communicatorFactory = CommunicatorFactory.getInstance();
        val communicatorMap = (ConcurrentHashMap<Object, Communicator>) ReflectionUtil.getFieldValue(
                CommunicatorFactory.class, COMMUNICATORS_FIELD, communicatorFactory);
        if (MapUtils.isEmpty(communicatorMap)) {
            log.warn("Cannot find instances of type Communicator.");
            return;
        }
        communicatorMap.forEach((k, v) -> {
            val executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(Communicator.class, THREAD_POOL_FIELD, v);
            if (Objects.isNull(executor)) {
                return;
            }
            val id = (String) ReflectionUtil.getFieldValue(Communicator.class, COMMUNICATOR_ID_FIELD, v);
            val executorWrapper = new ExecutorWrapper(id, executor);
            executorWrapper.setThreadPoolAliasName(v.getCommunicatorConfig().getLocator());
            initNotifyItems(id, executorWrapper);
            executors.put(id, executorWrapper);
        });
        log.info("DynamicTp adapter, tars executors init end, executors: {}", executors);
    }
}
