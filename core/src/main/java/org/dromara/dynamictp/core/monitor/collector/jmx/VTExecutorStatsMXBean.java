package org.dromara.dynamictp.core.monitor.collector.jmx;

import org.dromara.dynamictp.common.entity.VTExecutorStats;

import javax.management.MXBean;

/**
 * ClassName: VTExecutorStatsMXBean
 * Package: org.dromara.dynamictp.core.monitor.collector.jmx
 * Description:
 *
 * @author CYC
 * @create 2024/11/7 14:44
 */
@MXBean
public interface VTExecutorStatsMXBean {

    VTExecutorStats getVTExecutorStats();

    void setVTExecutorStats(VTExecutorStats vtExecutorStats);
}
