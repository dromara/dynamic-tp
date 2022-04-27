package com.dtp.adapter.rpc.dubbo;

import com.dtp.adapter.TpHandler;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.ThreadPoolStats;

import java.util.concurrent.Executor;

/**
 * DubboTpHandler related
 *
 * @author: yanhom
 * @since 1.0.6
 **/
public class DubboTpHandler implements TpHandler {

    @Override
    public Executor getTp() {
        // TODO see ThreadPoolStatusChecker
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
