package io.lyh.dtp.endpoint;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.RuntimeInfo;
import io.lyh.dtp.core.DtpExecutor;
import io.lyh.dtp.core.DtpRegistry;
import io.lyh.dtp.monitor.Metrics;
import io.lyh.dtp.monitor.JvmMetrics;
import io.lyh.dtp.monitor.MetricsHelper;
import com.google.common.collect.Lists;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.List;

/**
 * DtpEndpoint related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-30 15:27
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
