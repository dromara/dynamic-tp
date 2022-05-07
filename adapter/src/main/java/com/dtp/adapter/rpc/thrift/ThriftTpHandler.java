package com.dtp.adapter.rpc.thrift;

import com.dtp.adapter.TpHandler;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.ThreadPoolStats;

import java.util.concurrent.Executor;

/**
 * ThriftTpHandler related
 *
 * @author: yanhom
 * @since 1.0.6
 **/
public class ThriftTpHandler implements TpHandler {

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
