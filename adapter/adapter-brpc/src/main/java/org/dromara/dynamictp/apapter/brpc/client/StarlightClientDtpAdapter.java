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

package org.dromara.dynamictp.apapter.brpc.client;

import com.baidu.cloud.starlight.api.rpc.StarlightClient;
import com.baidu.cloud.starlight.api.rpc.threadpool.ThreadPoolFactory;
import com.baidu.cloud.starlight.core.rpc.SingleStarlightClient;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.jvmti.JVMTI;

import java.util.List;
import java.util.Objects;

/**
 * StarlightClientDtpAdapter related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
public class StarlightClientDtpAdapter extends AbstractDtpAdapter {

    private static final String TP_PREFIX = "brpcClientTp";

    private static final String THREAD_POOL_FIELD = "threadPoolOfAll";

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

        List<StarlightClient> starlightClients = JVMTI.getInstances(StarlightClient.class);
        if (CollectionUtils.isEmpty(starlightClients)) {
            log.warn("Cannot find beans of type StarlightClient.");
            return;
        }

        starlightClients.forEach(v -> {
            val threadPoolFactory = (ThreadPoolFactory) ReflectionUtil.getFieldValue(SingleStarlightClient.class,
                    THREAD_POOL_FIELD, v);
            if (Objects.isNull(threadPoolFactory)) {
                return;
            }
            String tpName = TP_PREFIX + "#" + v.remoteURI().getParameter("biz_thread_pool_name");
            val executor = threadPoolFactory.defaultThreadPool();
            if (Objects.nonNull(executor)) {
                enhanceOriginExecutor(tpName, executor, DEFAULT_THREAD_POOL_FIELD, threadPoolFactory);
            }
        });
    }
}
