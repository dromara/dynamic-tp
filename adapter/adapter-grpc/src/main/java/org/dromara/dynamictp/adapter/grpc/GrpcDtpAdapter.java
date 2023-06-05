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

import io.grpc.inprocess.InProcessSocketAddress;
import io.grpc.internal.InternalServer;
import io.grpc.internal.ServerImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.jvmti.JVMTI;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * GrpcDtpAdapter related
 *
 * @author yanhom
 * @author dragon-zhang
 * @since 1.0.9
 */
@Slf4j
public class GrpcDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "grpcTp";

    private static final String SERVER_FIELD = "transportServer";

    private static final String EXECUTOR_FIELD = "executor";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getGrpcTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        val beans = JVMTI.getInstances(ServerImpl.class);
        if (ArrayUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type ServerImpl.");
            return;
        }
        for (val serverImpl : beans) {
            val internalServer = (InternalServer) ReflectionUtil.getFieldValue(ServerImpl.class, SERVER_FIELD, serverImpl);
            String key = Optional.ofNullable(internalServer)
                    .map(server -> {
                        final SocketAddress address = server.getListenSocketAddress();
                        if (address instanceof InetSocketAddress) {
                            return String.valueOf(((InetSocketAddress) address).getPort());
                        } else if (address instanceof InProcessSocketAddress) {
                            return ((InProcessSocketAddress) address).getName();
                        }
                        return null;
                    }).orElse(null);
            if (Objects.isNull(key)) {
                return;
            }
            val executor = (Executor) ReflectionUtil.getFieldValue(ServerImpl.class, EXECUTOR_FIELD, serverImpl);
            String tpName = genTpName(key);
            if (Objects.nonNull(executor)) {
                val executorWrapper = new ExecutorWrapper(tpName, executor);
                initNotifyItems(tpName, executorWrapper);
                executors.put(tpName, executorWrapper);
            }
        }
        log.info("DynamicTp adapter, grpc server executors init end, executors: {}", executors);
    }

    private String genTpName(String key) {
        return NAME + "#" + key;
    }
}
