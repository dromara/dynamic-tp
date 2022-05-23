package com.dtp.adapter.dubbo.apache.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.dtp.adapter.dubbo.apache.DubboVersion;
import com.dtp.adapter.dubbo.common.AbstractDtpHandler;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.util.StreamUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.dubbo.common.Version;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.store.DataStore;
import org.apache.dubbo.common.threadpool.manager.DefaultExecutorRepository;
import org.apache.dubbo.common.threadpool.manager.ExecutorRepository;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static org.apache.dubbo.common.constants.CommonConstants.EXECUTOR_SERVICE_COMPONENT_KEY;

/**
 * ApacheDubboDtpHandler related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
@SuppressWarnings("all")
public class ApacheDubboDtpHandler extends AbstractDtpHandler {

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
        executors.forEach((k ,v) -> {
            val properties = tmpMap.get(k);
            updateBase(NAME, properties, (ThreadPoolExecutor) v);
        });
    }

    @Override
    public Map<String, ? extends Executor> getExecutors() {
        if (MapUtil.isNotEmpty(DUBBO_EXECUTORS)) {
            return DUBBO_EXECUTORS;
        }

        String currVersion = Version.getVersion();
        if (DubboVersion.compare(DubboVersion.VERSION_2_7_5, currVersion) > 0) {
            DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
            Map<String, Object> executors = dataStore.get(EXECUTOR_SERVICE_COMPONENT_KEY);
            if (MapUtil.isNotEmpty(executors)) {
                executors.forEach((k, v) -> DUBBO_EXECUTORS.put(genTpName(k), (ThreadPoolExecutor) v));
            }
            return DUBBO_EXECUTORS;
        }

        ExecutorRepository executorRepository;
        if (DubboVersion.compare(currVersion, DubboVersion.VERSION_3_0_3) >= 0) {
            executorRepository = ApplicationModel.defaultModel().getExtensionLoader(ExecutorRepository.class).getDefaultExtension();
        } else {
            executorRepository = ExtensionLoader.getExtensionLoader(ExecutorRepository.class).getDefaultExtension();
        }
        Field dataField = ReflectionUtils.findField(DefaultExecutorRepository.class, "data");
        if (Objects.isNull(dataField)) {
            return DUBBO_EXECUTORS;
        }
        ReflectionUtils.makeAccessible(dataField);
        val data = (ConcurrentMap<String, ConcurrentMap<Integer, ExecutorService>>) ReflectionUtils.getField(dataField, executorRepository);
        if (Objects.isNull(data)) {
            return DUBBO_EXECUTORS;
        }
        Map<Integer, ExecutorService> executorMap = data.get(EXECUTOR_SERVICE_COMPONENT_KEY);
        if (MapUtil.isNotEmpty(executorMap)) {
            executorMap.forEach((k, v) -> DUBBO_EXECUTORS.put(genTpName(k.toString()), (ThreadPoolExecutor) v));
        }
        return DUBBO_EXECUTORS;
    }

    private String genTpName(String port) {
        return NAME + "#" + port;
    }
}
