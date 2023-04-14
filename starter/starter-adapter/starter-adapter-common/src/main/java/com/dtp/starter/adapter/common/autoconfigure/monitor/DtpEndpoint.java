package com.dtp.starter.adapter.common.autoconfigure.monitor;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.RuntimeInfo;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.common.entity.JvmStats;
import com.dtp.common.entity.Metrics;
import com.dtp.core.DtpRegistry;
import com.dtp.core.converter.MetricsConverter;
import com.dtp.core.support.MetricsAware;
import com.dtp.core.thread.DtpExecutor;
import com.google.common.collect.Lists;
import lombok.val;
import org.apache.commons.collections.MapUtils;
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
        DtpRegistry.listAllDtpNames().forEach(x -> {
            DtpExecutor executor = DtpRegistry.getDtpExecutor(x);
            metricsList.add(MetricsConverter.convert(executor));
        });
        DtpRegistry.listAllCommonNames().forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getCommonExecutor(x);
            metricsList.add(MetricsConverter.convert(wrapper));
        });

        val handlerMap = ApplicationContextHolder.getBeansOfType(MetricsAware.class);
        if (MapUtils.isNotEmpty(handlerMap)) {
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
