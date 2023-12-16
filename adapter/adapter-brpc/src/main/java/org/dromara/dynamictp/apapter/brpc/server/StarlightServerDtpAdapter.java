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

package org.dromara.dynamictp.apapter.brpc.server;

import com.baidu.cloud.starlight.api.common.URI;
import com.baidu.cloud.starlight.api.rpc.StarlightServer;
import com.baidu.cloud.starlight.api.rpc.threadpool.ThreadPoolFactory;
import com.baidu.cloud.starlight.api.transport.ServerPeer;
import com.baidu.cloud.starlight.core.rpc.DefaultStarlightServer;
import com.baidu.cloud.starlight.core.rpc.ServerProcessor;
import com.baidu.cloud.starlight.transport.netty.NettyServer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.jvmti.JVMTI;

import java.util.Objects;

/**
 * StarlightServerDtpAdapter related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
public class StarlightServerDtpAdapter extends AbstractDtpAdapter {

    private static final String TP_PREFIX = "brpcServerTp";

    private static final String URI_FIELD = "uri";

    private static final String SERVER_PEER_FIELD = "serverPeer";

    private static final String THREAD_POOL_FACTORY_FIELD = "threadPoolFactory";

    private static final String DEFAULT_THREAD_POOL_FIELD = "defaultThreadPool";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(dtpProperties.getBrpcTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }

    @Override
    protected void initialize() {
        super.initialize();

        val bean = JVMTI.getInstance(StarlightServer.class);
        if (!(bean instanceof DefaultStarlightServer)) {
            return;
        }
        val starlightServer = (DefaultStarlightServer) bean;
        val uri = (URI) ReflectionUtil.getFieldValue(DefaultStarlightServer.class, URI_FIELD, starlightServer);
        val serverPeer = (ServerPeer) ReflectionUtil.getFieldValue(DefaultStarlightServer.class,
                SERVER_PEER_FIELD, starlightServer);

        if (Objects.isNull(uri) || Objects.isNull(serverPeer) || !(serverPeer instanceof NettyServer)) {
            return;
        }
        val processor = (ServerProcessor) serverPeer.getProcessor();
        if (Objects.isNull(processor)) {
            return;
        }
        val threadPoolFactory = (ThreadPoolFactory) ReflectionUtil.getFieldValue(ServerProcessor.class,
                THREAD_POOL_FACTORY_FIELD, processor);
        if (Objects.isNull(threadPoolFactory)) {
            return;
        }
        String tpName = TP_PREFIX + "#" + uri.getParameter("biz_thread_pool_name");
        val executor = threadPoolFactory.defaultThreadPool();
        if (Objects.nonNull(executor)) {
            enhanceOriginExecutor(tpName, executor, DEFAULT_THREAD_POOL_FIELD, threadPoolFactory);
        }
    }
}
