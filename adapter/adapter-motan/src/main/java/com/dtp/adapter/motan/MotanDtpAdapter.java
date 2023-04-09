package com.dtp.adapter.motan;

import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.ReflectionUtil;
import com.weibo.api.motan.config.springsupport.ServiceConfigBean;
import com.weibo.api.motan.protocol.rpc.DefaultRpcExporter;
import com.weibo.api.motan.rpc.Exporter;
import com.weibo.api.motan.transport.Server;
import com.weibo.api.motan.transport.netty.NettyServer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

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
                }
            });
        });
        log.info("DynamicTp adapter, motan server executors init end, executors: {}", executors);
    }
}
