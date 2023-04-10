package com.dtp.core.monitor;

import cn.hutool.core.io.FileUtil;
import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.entity.JvmStats;
import com.dtp.common.entity.Metrics;
import com.dtp.core.DtpRegistry;
import com.dtp.core.converter.MetricsConverter;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.support.MetricsAware;
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
        DtpRegistry.listAllExecutorNames().forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(x);
            metricsList.add(MetricsConverter.convert(wrapper));
        });

        val handlerMap = ApplicationContextHolder.getBeansOfType(MetricsAware.class);
        if (MapUtils.isNotEmpty(handlerMap)) {
            handlerMap.forEach((k, v) -> metricsList.addAll(v.getMultiPoolStats()));
        }
        JvmStats jvmStats = new JvmStats();
        Runtime runtime = Runtime.getRuntime();
        jvmStats.setMaxMemory(FileUtil.readableFileSize(runtime.maxMemory()));
        jvmStats.setTotalMemory(FileUtil.readableFileSize(runtime.totalMemory()));
        jvmStats.setFreeMemory(FileUtil.readableFileSize(runtime.freeMemory()));
        jvmStats.setUsableMemory(FileUtil.readableFileSize(runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory()));
        metricsList.add(jvmStats);
        return metricsList;
    }
}
