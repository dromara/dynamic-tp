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

package org.dromara.dynamictp.adapter.dubbo.alibaba;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.store.DataStore;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.spring.ApplicationContextHolder;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.support.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alibaba.dubbo.common.Constants.EXECUTOR_SERVICE_COMPONENT_KEY;

/**
 * AlibabaDubboDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
@SuppressWarnings("all")
public class AlibabaDubboDtpAdapter extends AbstractDtpAdapter implements InitializingBean {

    private static final String NAME = "dubboTp";
    
    private final AtomicBoolean registered = new AtomicBoolean(false);
    
    @Override
    public void afterPropertiesSet() throws Exception {
        //从ApplicationReadyEvent改为ContextRefreshedEvent后，
        //启动时无法dubbo获取线程池，这里直接每隔1s轮循，直至成功初始化线程池
        Executors.newCachedThreadPool().submit(() -> {
            while (!registered.get()) {
                try {
                    Thread.sleep(1000);
                    final DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
                    this.initialize();
                    this.refresh(dtpProperties);
                } catch (Throwable e) {
                }
            }
        });
    }
    
    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getDubboTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();
        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
        Map<String, Object> executorMap = dataStore.get(EXECUTOR_SERVICE_COMPONENT_KEY);
        if (MapUtils.isNotEmpty(executorMap) && registered.compareAndSet(false, true)) {
            executorMap.forEach((k, v) -> {
                val name = genTpName(k);
                val executorWrapper = new ExecutorWrapper(name, (ThreadPoolExecutor) v);
                initNotifyItems(name, executorWrapper);
                executors.put(name, executorWrapper);
                executorMap.replace(k, new ThreadPoolExecutorProxy(executorWrapper));
            });
        }
        log.info("DynamicTp adapter, alibaba dubbo provider executors init end, executors: {}", executors);
    }

    private String genTpName(String port) {
        return NAME + "#" + port;
    }
}
