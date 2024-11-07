package org.dromara.dynamictp.core.monitor.collector.jmx;

import org.dromara.dynamictp.common.entity.VTExecutorStats;

/**
 * ClassName: VTExecutorStatsJMX
 * Package: org.dromara.dynamictp.core.monitor.collector.jmx
 * Description:
 *
 * @author CYC
 * @create 2024/11/5 18:29
 */
public class VTExecutorStatsJMX implements VTExecutorStatsMXBean{

    private VTExecutorStats vtExecutorStats;
    public VTExecutorStatsJMX(VTExecutorStats vtExecutorStats) {
        this.vtExecutorStats = vtExecutorStats;
    }

    @Override
    public VTExecutorStats getVTExecutorStats() {
        return vtExecutorStats;
    }

    @Override
    public void setVTExecutorStats(VTExecutorStats vtExecutorStats) {
        this.vtExecutorStats = vtExecutorStats;
    }
}
