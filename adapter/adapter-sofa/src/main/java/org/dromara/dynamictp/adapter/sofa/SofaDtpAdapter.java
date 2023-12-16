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

package org.dromara.dynamictp.adapter.sofa;

import com.alipay.sofa.rpc.config.ServerConfig;
import com.alipay.sofa.rpc.config.UserThreadPoolManager;
import com.alipay.sofa.rpc.server.Server;
import com.alipay.sofa.rpc.server.ServerFactory;
import com.alipay.sofa.rpc.server.UserThreadPool;
import com.alipay.sofa.rpc.server.bolt.BoltServer;
import com.alipay.sofa.rpc.server.http.AbstractHttpServer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * SofaDtpAdapter related
 *
 * @author yanhom
 * @since 1.1.0
 */
@SuppressWarnings("all")
@Slf4j
public class SofaDtpAdapter extends AbstractDtpAdapter {

    private static final String TP_PREFIX = "sofaTp";

    private static final String SERVER_CONFIG_FIELD = "serverConfig";

    private static final String USER_THREAD_FIELD = "userThreadMap";

    private static final String USER_THREAD_EXECUTOR_FIELD = "executor";

    private static final String BIZ_THREAD_POOL = "bizThreadPool";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(dtpProperties.getSofaTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }

    @Override
    protected void initialize() {
        super.initialize();

        List<Server> servers = ServerFactory.getServers();
        boolean hasUserThread = UserThreadPoolManager.hasUserThread();
        if (CollectionUtils.isEmpty(servers) && !hasUserThread) {
            log.warn("Empty servers and empty user thread pools.");
            return;
        }
        servers.forEach(v -> {
            ThreadPoolExecutor executor = null;
            ServerConfig serverConfig = null;
            if (v instanceof BoltServer) {
                BoltServer server = (BoltServer) v;
                executor = server.getBizThreadPool();
                serverConfig = (ServerConfig) ReflectionUtil.getFieldValue(BoltServer.class,
                        SERVER_CONFIG_FIELD, server);
            } else if (v instanceof AbstractHttpServer) {
                AbstractHttpServer server = (AbstractHttpServer) v;
                executor = server.getBizThreadPool();
                serverConfig = (ServerConfig) ReflectionUtil.getFieldValue(AbstractHttpServer.class,
                        SERVER_CONFIG_FIELD, server);
            }
            if (Objects.isNull(executor) || Objects.isNull(serverConfig)) {
                return;
            }
            String tpName = TP_PREFIX + "#" + serverConfig.getProtocol() + "#" + serverConfig.getPort();
            enhanceOriginExecutor(tpName, executor, BIZ_THREAD_POOL, v);
        });

        if (hasUserThread) {
            handleUserThreadPools();
        }
    }

    private void handleUserThreadPools() {
        try {
            Field f = UserThreadPoolManager.class.getDeclaredField(USER_THREAD_FIELD);
            f.setAccessible(true);
            val userThreadMap = (Map<String, UserThreadPool>) f.get(null);
            if (MapUtils.isNotEmpty(userThreadMap)) {
                userThreadMap.forEach((k, v) -> {
                    String tpName = TP_PREFIX + "#" + k;
                    enhanceOriginExecutor(tpName, v.getExecutor(), USER_THREAD_FIELD, v);
                });
            }
        } catch (Exception e) {
            log.warn("UserThreadPoolManager handles failed", e);
        }
    }
}
