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

package org.dromara.dynamictp.adapter.grpc;

import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.ApplicationContextHolder;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import io.grpc.internal.ServerImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;
import org.apache.commons.collections4.MapUtils;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * GrpcDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.9
 */
@Slf4j
public class GrpcDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "grpcTp";

    private static final String SERVER_FIELD = "server";

    private static final String EXECUTOR_FIELD = "executor";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getGrpcTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        val beans = ApplicationContextHolder.getBeansOfType(GrpcServerLifecycle.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type GrpcServerLifecycle.");
            return;
        }
        beans.forEach((k, v) -> {
            val server = ReflectionUtil.getFieldValue(GrpcServerLifecycle.class, SERVER_FIELD, v);
            if (Objects.isNull(server)) {
                return;
            }
            val serverImpl = (ServerImpl) server;
            val executor = (Executor) ReflectionUtil.getFieldValue(ServerImpl.class, EXECUTOR_FIELD, serverImpl);
            String tpName = genTpName(k);
            if (Objects.nonNull(executor)) {
                val executorWrapper = new ExecutorWrapper(tpName, executor);
                initNotifyItems(tpName, executorWrapper);
                executors.put(tpName, executorWrapper);
            }
        });
        log.info("DynamicTp adapter, grpc server executors init end, executors: {}", executors);
    }

    /**
     * Gen tp name.
     *
     * @param serverLifeCycleName (shadedNettyGrpcServerLifecycle / inProcessGrpcServerLifecycle / nettyGrpcServerLifecycle)
     * @return tp name
     */
    private String genTpName(String serverLifeCycleName) {
        return serverLifeCycleName.replace("GrpcServerLifecycle", "Tp");
    }
}
