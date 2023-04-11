package com.dtp.adapter.sofa;

import com.alipay.sofa.rpc.config.ServerConfig;
import com.alipay.sofa.rpc.config.UserThreadPoolManager;
import com.alipay.sofa.rpc.server.Server;
import com.alipay.sofa.rpc.server.ServerFactory;
import com.alipay.sofa.rpc.server.UserThreadPool;
import com.alipay.sofa.rpc.server.bolt.BoltServer;
import com.alipay.sofa.rpc.server.http.AbstractHttpServer;
import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.common.properties.DtpProperties;
import com.dtp.common.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

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

    private static final String NAME = "sofaTp";

    private static final String SERVER_CONFIG_FIELD_NAME = "serverConfig";

    private static final String USER_THREAD_FIELD_NAME = "userThreadMap";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getSofaTp(), dtpProperties.getPlatforms());
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
                        SERVER_CONFIG_FIELD_NAME, server);
            } else if (v instanceof AbstractHttpServer) {
                AbstractHttpServer server = (AbstractHttpServer) v;
                executor = server.getBizThreadPool();
                serverConfig = (ServerConfig) ReflectionUtil.getFieldValue(AbstractHttpServer.class,
                        SERVER_CONFIG_FIELD_NAME, server);
            }
            if (Objects.isNull(executor) || Objects.isNull(serverConfig)) {
                return;
            }

            String key = NAME + "#" + serverConfig.getProtocol() + "#" + serverConfig.getPort();
            val executorWrapper = new ExecutorWrapper(key, executor);
            initNotifyItems(key, executorWrapper);
            executors.put(key, executorWrapper);
        });

        if (hasUserThread) {
            handleUserThreadPools();
        }
        log.info("DynamicTp adapter, sofa executors init end, executors: {}", executors);
    }

    private void handleUserThreadPools() {
        try {
            Field f = UserThreadPoolManager.class.getDeclaredField(USER_THREAD_FIELD_NAME);
            f.setAccessible(true);
            val userThreadMap = (Map<String, UserThreadPool>) f.get(null);
            if (MapUtils.isNotEmpty(userThreadMap)) {
                userThreadMap.forEach((k, v) -> {
                    val executorWrapper = new ExecutorWrapper(k, v.getExecutor());
                    initNotifyItems(k, executorWrapper);
                    executors.put(k, executorWrapper);
                });
            }
        } catch (Exception e) {
            log.warn("UserThreadPoolManager handles failed", e);
        }
    }
}
