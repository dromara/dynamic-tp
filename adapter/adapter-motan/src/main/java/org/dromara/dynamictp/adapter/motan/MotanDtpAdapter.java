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

package org.dromara.dynamictp.adapter.motan;

import cn.hutool.core.exceptions.ExceptionUtil;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.spring.ApplicationContextHolder;
import org.dromara.dynamictp.core.ThreadPoolExecutorProxy;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import com.weibo.api.motan.config.springsupport.ServiceConfigBean;
import com.weibo.api.motan.protocol.rpc.DefaultRpcExporter;
import com.weibo.api.motan.rpc.Exporter;
import com.weibo.api.motan.transport.Server;
import com.weibo.api.motan.transport.netty.NettyServer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * MotanDtpAdapter related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
public class MotanDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "motanTp";

    private static final String SERVER_FIELD_NAME = "server";

    private static final String EXECUTOR_FIELD_NAME = "standardThreadExecutor";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getMotanTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();

        val beans = ApplicationContextHolder.getBeansOfType(ServiceConfigBean.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type ServiceConfigBean.");
            return;
        }
        beans.forEach((k, v) -> {
            @SuppressWarnings("unchecked")
            List<Exporter<?>> exporters = v.getExporters();
            if (CollectionUtils.isEmpty(exporters)) {
                return;
            }
            exporters.forEach(e -> {
                if (!(e instanceof DefaultRpcExporter)) {
                    return;
                }
                val defaultRpcExporter = (DefaultRpcExporter<?>) e;
                val server = (Server) ReflectionUtil.getFieldValue(DefaultRpcExporter.class, SERVER_FIELD_NAME, defaultRpcExporter);
                if (Objects.isNull(server) || !(server instanceof NettyServer)) {
                    return;
                }
                val nettyServer = (NettyServer) server;
                val executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(NettyServer.class, EXECUTOR_FIELD_NAME, nettyServer);
                if (Objects.nonNull(executor)) {
                    String key = NAME + "#" + nettyServer.getUrl().getPort();
                    val executorWrapper = new ExecutorWrapper(key, executor);
                    initNotifyItems(key, executorWrapper);
                    executors.put(key, executorWrapper);
                    ThreadPoolExecutorProxy proxy = new ThreadPoolExecutorProxy(executorWrapper);
                    try {
                        ReflectionUtil.setFieldValue(NettyServer.class, EXECUTOR_FIELD_NAME, nettyServer, proxy);
                    } catch (IllegalAccessException ex) {
                        log.error(ExceptionUtil.stacktraceToOneLineString(ex));
                    }
                }
            });
        });
        log.info("DynamicTp adapter, motan server executors init end, executors: {}", executors);
    }
}
