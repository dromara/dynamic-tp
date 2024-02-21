/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.adapter.dubbo.apache;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.Version;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.store.DataStore;
import org.apache.dubbo.common.threadpool.manager.DefaultExecutorRepository;
import org.apache.dubbo.common.threadpool.manager.ExecutorRepository;
import org.apache.dubbo.common.threadpool.support.eager.EagerThreadPoolExecutor;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.apache.dubbo.remoting.transport.dispatcher.WrappedChannelHandler;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.spring.ApplicationContextHolder;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.jvmti.JVMTI;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER_SIDE;
import static org.apache.dubbo.common.constants.CommonConstants.SIDE_KEY;

/**
 * ApacheDubboDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
@SuppressWarnings("all")
public class ApacheDubboDtpAdapter extends AbstractDtpAdapter {

    private static final String TP_PREFIX = "dubboTp";

    private static final String EXECUTOR_SERVICE_COMPONENT_KEY = ExecutorService.class.getName();

    private static final String EXECUTOR_FILED_NAME = "executor";

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ServiceBeanExportedEvent) {
            try {
                DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
                initialize();
                refresh(dtpProperties);
            } catch (Exception e) {
                log.error("DynamicTp adapter, {} init failed.", getTpPrefix(), e);
            }
        }
    }

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(dtpProperties.getDubboTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }

    @Override
    protected void initialize() {
        super.initialize();
        String currVersion = Version.getVersion();
        if (DubboVersion.compare(DubboVersion.VERSION_2_7_5, currVersion) > 0) {
            // 当前dubbo版本 < 2.7.5
            DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
            if (Objects.isNull(dataStore)) {
                return;
            }
            Map<String, Object> executorMap = dataStore.get(EXECUTOR_SERVICE_COMPONENT_KEY);
            //获取WrappedChannelHandler实例,有Client消费者和Server提供者两个实例
            List<WrappedChannelHandler> wrappedChannelHandlerList = JVMTI.getInstances(WrappedChannelHandler.class);
            if (MapUtils.isNotEmpty(executorMap) && CollectionUtils.isNotEmpty(wrappedChannelHandlerList)) {
                for (WrappedChannelHandler wrappedChannelHandler : wrappedChannelHandlerList) {
                    URL url = wrappedChannelHandler.getUrl();
                    //消费者线程池不做替换,与下方高版本逻辑保持一致
                    if (CONSUMER_SIDE.equalsIgnoreCase(url.getParameter(SIDE_KEY))) {
                        continue;
                    }
                    executorMap.forEach((k, v) -> {
                        ThreadPoolExecutor proxy = getProxy((ThreadPoolExecutor) v);
                        dataStore.put(EXECUTOR_SERVICE_COMPONENT_KEY, k, proxy);
                        try {
                            //修改为动态线程池proxy
                            ReflectionUtil.setFieldValue(EXECUTOR_FILED_NAME, wrappedChannelHandler, proxy);
                        } catch (IllegalAccessException e) {
                            log.error("Dynamic tp update dubbo tp failed, port={}", k, e);
                        }
                        putAndFinalize(genTpName(k), (ExecutorService) v, proxy);
                    });
                }
            }
            return;
        }

        ExecutorRepository executorRepository;
        if (DubboVersion.compare(currVersion, DubboVersion.VERSION_3_0_3) >= 0) {
            // 当前dubbo版本 >= 3.0.3
            executorRepository = ApplicationModel.defaultModel().getExtensionLoader(ExecutorRepository.class).getDefaultExtension();
        } else {
            // 2.7.5 <= 当前dubbo版本 < 3.0.3
            executorRepository = ExtensionLoader.getExtensionLoader(ExecutorRepository.class).getDefaultExtension();
        }

        val data = (ConcurrentMap<String, ConcurrentMap<Object, ExecutorService>>) ReflectionUtil.getFieldValue(
                DefaultExecutorRepository.class, "data", executorRepository);
        if (Objects.isNull(data)) {
            return;
        }

        Map<Object, ExecutorService> executorMap = data.get(EXECUTOR_SERVICE_COMPONENT_KEY);
        if (MapUtils.isNotEmpty(executorMap)) {
            executorMap.forEach((k, v) -> {
                ThreadPoolExecutor proxy = getProxy(v);
                executorMap.replace(k, proxy);
                putAndFinalize(genTpName(k.toString()), (ExecutorService) v, proxy);
            });
        }
    }
    private ThreadPoolExecutor getProxy(Executor executor) {
        ThreadPoolExecutor proxy;
        if (executor instanceof EagerThreadPoolExecutor) {
            proxy = new EagerThreadPoolExecutorProxy((EagerThreadPoolExecutor) executor);
        } else {
            proxy = new ThreadPoolExecutorProxy((ThreadPoolExecutor) executor);
        }
        return proxy;
    }

    private String genTpName(String port) {
        return TP_PREFIX + "#" + port;
    }
}
