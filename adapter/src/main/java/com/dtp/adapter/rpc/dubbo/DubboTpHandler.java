package com.dtp.adapter.rpc.dubbo;

import com.dtp.adapter.TpHandler;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.dto.ThreadPoolStats;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * DubboTpHandler related
 *
 * @author: yanhom
 * @since 1.0.6
 **/
public class DubboTpHandler implements TpHandler {

    @Override
    public List<Executor> getExecutors() {
        // TODO see ThreadPoolStatusChecker
        return null;
    }

    @Override
    public void updateTp(DtpProperties dtpProperties) {
        // TODO
    }

    @Override
    public List<ThreadPoolStats> getMultiPoolStats() {
        // TODO
        return null;
    }
}
