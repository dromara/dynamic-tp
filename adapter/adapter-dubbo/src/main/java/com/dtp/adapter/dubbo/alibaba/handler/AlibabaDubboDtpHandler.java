package com.dtp.adapter.dubbo.alibaba.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.store.DataStore;
import com.dtp.adapter.common.AbstractDtpHandler;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.util.StreamUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static com.alibaba.dubbo.common.Constants.EXECUTOR_SERVICE_COMPONENT_KEY;


/**
 * AlibabaDubboDtpHandler related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
@SuppressWarnings("all")
public class AlibabaDubboDtpHandler extends AbstractDtpHandler {

    private static final String NAME = "dubboTp";

    private static final Map<String, ThreadPoolExecutor> DUBBO_EXECUTORS = Maps.newHashMap();

    @Override
    public void updateTp(DtpProperties dtpProperties) {
        val dubboTpList = dtpProperties.getDubboTp();
        val executors = getExecutors();
        if (CollUtil.isEmpty(dubboTpList) || CollUtil.isEmpty(executors)) {
            return;
        }

        val tmpMap = StreamUtil.toMap(dubboTpList, SimpleTpProperties::getThreadPoolName);
        executors.forEach((k ,v) -> updateBase(NAME, tmpMap.get(k), (ThreadPoolExecutor) v));
    }

    @Override
    public Map<String, ? extends Executor> getExecutors() {
        if (MapUtil.isNotEmpty(DUBBO_EXECUTORS)) {
            return DUBBO_EXECUTORS;
        }

        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
        Map<String, Object> executors = dataStore.get(EXECUTOR_SERVICE_COMPONENT_KEY);
        if (MapUtil.isNotEmpty(executors)) {
            executors.forEach((k, v) -> DUBBO_EXECUTORS.put(genTpName(k), (ThreadPoolExecutor) v));
        }

        log.info("DynamicTp adapter, alibaba dubbo executors init end, executors: {}", DUBBO_EXECUTORS);
        return DUBBO_EXECUTORS;
    }

    private String genTpName(String port) {
        return NAME + "#" + port;
    }
}
