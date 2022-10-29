package com.dtp.adapter.grpc;

import cn.hutool.core.collection.CollUtil;
import com.dtp.adapter.common.AbstractDtpAdapter;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.util.ReflectionUtil;
import io.grpc.internal.ServerImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;

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

    private static final String NAME = "grpcServerTp";

    private static final String SERVER_FIELD = "server";

    private static final String EXECUTOR_FIELD = "executor";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getGrpcTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        val beans = ApplicationContextHolder.getBeansOfType(GrpcServerLifecycle.class);
        if (CollUtil.isEmpty(beans)) {
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
            if (Objects.nonNull(executor)) {
                val executorWrapper = new ExecutorWrapper(NAME, executor);
                initNotifyItems(NAME, executorWrapper);
                EXECUTORS.put(NAME, executorWrapper);
            }
        });
        log.info("DynamicTp adapter, grpc server executors init end, executors: {}", EXECUTORS);
    }
}
