package com.dtp.adapter.rpc.grpc;

import com.dtp.adapter.TpHandler;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.ThreadPoolStats;

import java.util.concurrent.Executor;

/**
 * GrpcTpHandler related
 *
 * @author: yanhom
 * @since 1.0.6
 **/
public class GrpcTpHandler implements TpHandler {

    @Override
    public Executor getExecutor() {
        // TODO
        return null;
    }

    @Override
    public void updateTp(DtpProperties dtpProperties) {
        // TODO
    }

    @Override
    public ThreadPoolStats getPoolStats() {
        // TODO
        return null;
    }
}
