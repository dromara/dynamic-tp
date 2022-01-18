package io.lyh.dynamic.tp.core.monitor.endpoint;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.RuntimeInfo;
import com.google.common.collect.Lists;
import io.lyh.dynamic.tp.common.dto.JvmMetrics;
import io.lyh.dynamic.tp.common.dto.Metrics;
import io.lyh.dynamic.tp.core.DtpExecutor;
import io.lyh.dynamic.tp.core.DtpRegistry;
import io.lyh.dynamic.tp.core.helper.MetricsHelper;
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
            DtpExecutor dtpExecutor = DtpRegistry.getExecutor(x);
            metricsList.add(MetricsHelper.getMetrics(dtpExecutor));
        });

        JvmMetrics jvmMetrics = new JvmMetrics();
        RuntimeInfo runtimeInfo = new RuntimeInfo();
        jvmMetrics.setMaxMemory(FileUtil.readableFileSize(runtimeInfo.getMaxMemory()));
        jvmMetrics.setTotalMemory(FileUtil.readableFileSize(runtimeInfo.getTotalMemory()));
        jvmMetrics.setFreeMemory(FileUtil.readableFileSize(runtimeInfo.getFreeMemory()));
        jvmMetrics.setUsableMemory(FileUtil.readableFileSize(runtimeInfo.getUsableMemory()));
        metricsList.add(jvmMetrics);
        return metricsList;
    }
}
