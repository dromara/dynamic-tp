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

import com.weibo.api.motan.config.springsupport.ServiceConfigBean;
import com.weibo.api.motan.protocol.rpc.DefaultRpcExporter;
import com.weibo.api.motan.rpc.Exporter;
import com.weibo.api.motan.transport.Server;
import com.weibo.api.motan.transport.netty.NettyServer;
import com.weibo.api.motan.transport.netty.StandardThreadExecutor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.util.ReflectionUtil;

import java.util.List;
import java.util.Objects;

/**
 * MotanDtpAdapter related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
@SuppressWarnings("all")
public class MotanDtpAdapter extends AbstractDtpAdapter {

    private static final String TP_PREFIX = "motanTp";

    private static final String SERVER_FIELD = "server";

    private static final String EXECUTOR_FIELD = "standardThreadExecutor";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(dtpProperties.getMotanTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected String getTpPrefix() {
        return TP_PREFIX;
    }

    @Override
    protected void initialize() {
        super.initialize();

        val beans = ContextManagerHelper.getBeansOfType(ServiceConfigBean.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type ServiceConfigBean.");
            return;
        }
        beans.forEach((k, v) -> {
            List<Exporter<?>> exporters = v.getExporters();
            if (CollectionUtils.isEmpty(exporters)) {
                return;
            }
            exporters.forEach(e -> {
                if (!(e instanceof DefaultRpcExporter)) {
                    return;
                }
                val defaultRpcExporter = (DefaultRpcExporter<?>) e;
                val server = (Server) ReflectionUtil.getFieldValue(DefaultRpcExporter.class, SERVER_FIELD, defaultRpcExporter);
                if (Objects.isNull(server) || !(server instanceof NettyServer)) {
                    return;
                }
                val nettyServer = (NettyServer) server;
                val executor = (StandardThreadExecutor) ReflectionUtil.getFieldValue(NettyServer.class, EXECUTOR_FIELD, nettyServer);
                if (Objects.nonNull(executor)) {
                    StandardThreadExecutorProxy proxy = new StandardThreadExecutorProxy(executor);
                    String tpName = TP_PREFIX + "#" + nettyServer.getUrl().getPort();
                    ReflectionUtil.setFieldValue(EXECUTOR_FIELD, nettyServer, proxy);
                    putAndFinalize(tpName, executor, proxy);
                }
            });
        });
    }
}
