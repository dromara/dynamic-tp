package com.dtp.core.monitor.endpoint;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.RuntimeInfo;
import com.dtp.common.dto.JvmStats;
import com.dtp.common.dto.Metrics;
import com.dtp.core.DtpRegistry;
import com.dtp.core.convert.MetricsConverter;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.core.thread.DtpExecutor;
import com.google.common.collect.Lists;
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
        List<String> commonNames = DtpRegistry.listAllCommonNames();

        List<Metrics> metricsList = Lists.newArrayList();
        dtpNames.forEach(x -> {
            DtpExecutor executor = DtpRegistry.getDtpExecutor(x);
            metricsList.add(MetricsConverter.convert(executor));
        });

        commonNames.forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getCommonExecutor(x);
            metricsList.add(MetricsConverter.convert(wrapper));
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
