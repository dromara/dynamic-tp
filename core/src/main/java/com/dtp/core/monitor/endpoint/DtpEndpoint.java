package com.dtp.core.monitor.endpoint;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.RuntimeInfo;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.DtpRegistry;
import com.dtp.core.convert.MetricsConverter;
import com.google.common.collect.Lists;
import com.dtp.common.dto.JvmStats;
import com.dtp.common.dto.Metrics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.List;

/**
 * DtpEndpoint related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Endpoint(id = "dynamic-tp")
public class DtpEndpoint {

    @ReadOperation
    public List<Metrics> invoke() {

        List<String> dtpNames = DtpRegistry.listAllDtpNames();
        List<Metrics> metricsList = Lists.newArrayList();
        dtpNames.forEach(x -> {
            DtpExecutor executor = DtpRegistry.getExecutor(x);
            metricsList.add(MetricsConverter.convert(executor,
                    executor.getThreadPoolName(), executor.getRejectCount()));
        });

        JvmStats jvmStats = new JvmStats();
        RuntimeInfo runtimeInfo = new RuntimeInfo();
        jvmStats.setMaxMemory(FileUtil.readableFileSize(runtimeInfo.getMaxMemory()));
        jvmStats.setTotalMemory(FileUtil.readableFileSize(runtimeInfo.getTotalMemory()));
        jvmStats.setFreeMemory(FileUtil.readableFileSize(runtimeInfo.getFreeMemory()));
        jvmStats.setUsableMemory(FileUtil.readableFileSize(runtimeInfo.getUsableMemory()));
        metricsList.add(jvmStats);
        return metricsList;
    }
}
