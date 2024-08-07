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

    private static final String EXECUTOR_FIELD = "executor";

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ServiceBeanExportedEvent) {
            try {
                DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
                initialize();
                afterInitialize();
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
            val handlers = JVMTI.getInstances(WrappedChannelHandler.class);
            if (CollectionUtils.isEmpty(handlers)) {
                return;
            }
            DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
            handlers.forEach(handler -> {
                //获取WrappedChannelHandler中的原始线程池
                val originExecutor = ReflectionUtil.getFieldValue(EXECUTOR_FIELD, handler);
                if (!(originExecutor instanceof ExecutorService)) {
                    return;
                }
                URL url = handler.getUrl();
                //低版本跳过消费者线程池配置
                if (!CONSUMER_SIDE.equalsIgnoreCase(url.getParameter(SIDE_KEY))) {
                    String port = String.valueOf(url.getPort());
                    String tpName = genTpName(port);
                    //增强原始线程池,替换为动态线程池代理
                    enhanceOriginExecutor(tpName, (ThreadPoolExecutor) originExecutor, EXECUTOR_FIELD, handler);
                    //获取增强后的新动态线程池
                    Object newExexutor = ReflectionUtil.getFieldValue(EXECUTOR_FIELD, handler);
                    //替换dataStore中的线程池
                    dataStore.put(EXECUTOR_SERVICE_COMPONENT_KEY, port, newExexutor);
                }
            });
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
