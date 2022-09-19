package com.dtp.core.monitor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.system.RuntimeInfo;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.JvmStats;
import com.dtp.common.dto.Metrics;
import com.dtp.core.DtpRegistry;
import com.dtp.core.adapter.DtpAdapter;
import com.dtp.core.convert.MetricsConverter;
import com.dtp.core.thread.DtpExecutor;
import com.google.common.collect.Lists;
import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.List;

/**
 * DtpEndpoint related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Endpoint(id = "dynamic-tp")
public class DtpEndpoint {

    @ReadOperation
    public List<Metrics> invoke() {

        List<Metrics> metricsList = Lists.newArrayList();
        List<String> dtpNames = DtpRegistry.listAllDtpNames();
        dtpNames.forEach(x -> {
            DtpExecutor executor = DtpRegistry.getDtpExecutor(x);
            metricsList.add(MetricsConverter.convert(executor));
        });

        List<String> commonNames = DtpRegistry.listAllCommonNames();
        commonNames.forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getCommonExecutor(x);
            metricsList.add(MetricsConverter.convert(wrapper));
        });

        val handlerMap = ApplicationContextHolder.getBeansOfType(DtpAdapter.class);
        if (CollUtil.isNotEmpty(handlerMap)) {
            handlerMap.forEach((k, v) -> metricsList.addAll(v.getMultiPoolStats()));
        }

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
