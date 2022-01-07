package io.lyh.dtp.core;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.ttl.threadpool.TtlExecutors;
import io.lyh.dtp.common.em.QueueTypeEnum;
import io.lyh.dtp.common.em.RejectedTypeEnum;
import io.lyh.dtp.domain.NotifyItem;
import io.lyh.dtp.handler.reject.RejectedCountableCallerRunsPolicy;
import io.lyh.dtp.support.VariableLinkedBlockingQueue;
import io.lyh.dtp.common.constant.DynamicTpConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.*;

/**
 * ThreadPoolBuilder related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-27 18:02
 * @since 1.0.0
 **/
public class ThreadPoolBuilder {

    /**
     * 线程名称
     */
    private String threadPoolName = "DynamicTp";

    /**
     * 核心线程数
     */
    private int corePoolSize = 2;

    /**
     * 最大线程数
     */
    private int maximumPoolSize = DynamicTpConst.AVAILABLE_PROCESSORS;

    /**
     * 线程存活时间
     */
    private long keepAliveTime = 30;

    /**
     * 存活时间单位
     */
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    /**
     * 阻塞队列
     * @see QueueTypeEnum
     */
    private BlockingQueue workQueue = new VariableLinkedBlockingQueue(1024);

    /**
     * 线程池任务满时拒绝任务策略
     * @see RejectedTypeEnum
     */
    private RejectedExecutionHandler rejectedExecutionHandler = new RejectedCountableCallerRunsPolicy();

    /**
     * 线程工厂
     */
    private ThreadFactory threadFactory = new NamedThreadFactory("dynamic-tp");

    /**
     * 允许核心线程超时
     */
    private boolean allowCoreThreadTimeOut = false;

    /**
     * 动态线程池 or 普通线程池
     */
    private boolean dynamic = true;

    /**
     * 通知告警项
     */
    private List<NotifyItem> notifyItems = NotifyItem.getDefaultNotifyItems();

    private ThreadPoolBuilder() {}

    public static ThreadPoolBuilder newBuilder() {
        return new ThreadPoolBuilder();
    }

    public ThreadPoolBuilder threadPoolName(String poolName) {
        this.threadPoolName = poolName;
        return this;
    }

    public ThreadPoolBuilder corePoolSize(int corePoolSize) {
        if (corePoolSize >= 0) {
            this.corePoolSize = corePoolSize;
        }
        return this;
    }

    public ThreadPoolBuilder maximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize > 0) {
            this.maximumPoolSize = maximumPoolSize;
        }
        return this;
    }

    public ThreadPoolBuilder keepAliveTime(long keepAliveTime) {
        if (keepAliveTime > 0) {
            this.keepAliveTime = keepAliveTime;
        }
        return this;
    }

    public ThreadPoolBuilder timeUnit(TimeUnit timeUnit) {
        if (timeUnit != null) {
            this.timeUnit = timeUnit;
        }
        return this;
    }

    /**
     * create work queue
     * @param queueName queue name
     * @param capacity queue capacity, default 128
     * @param fair For SynchronousQueue
     * @return
     */
    public ThreadPoolBuilder workQueue(String queueName, Integer capacity, Boolean fair) {
        if (StringUtils.isNotBlank(queueName)) {
            workQueue = QueueTypeEnum.buildBlockingQueue(queueName,
                    capacity != null ? capacity : 128, fair != null && fair);
        }
        return this;
    }

    public ThreadPoolBuilder rejectedExecutionHandler(String rejectedName) {
        if (StringUtils.isNotBlank(rejectedName)) {
            rejectedExecutionHandler = RejectedTypeEnum.buildRejectedHandler(rejectedName);
        }
        return this;
    }

    public ThreadPoolBuilder threadFactory(String prefix) {
        if (StringUtils.isNotBlank(prefix)) {
            threadFactory = new NamedThreadFactory(prefix);
        }
        return this;
    }

    public ThreadPoolBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        return this;
    }

    public ThreadPoolBuilder dynamic(boolean dynamic) {
        this.dynamic = dynamic;
        return this;
    }

    public ThreadPoolBuilder notifyItems(List<NotifyItem> notifyItemList) {
        if (CollUtil.isNotEmpty(notifyItemList)) {
            notifyItems = notifyItemList;
        }
        return this;
    }

    /**
     * Build according to dynamic field.
     * @return
     */
    public ThreadPoolExecutor build() {
        if (dynamic) {
            return buildDtpExecutor(this);
        } else {
            return buildCommonExecutor(this);
        }
    }

    /**
     * Build dynamic ThreadPoolExecutor.
     * @return
     */
    public DtpExecutor buildDynamic() {
        return buildDtpExecutor(this);
    }

    /**
     * Build common ThreadPoolExecutor.
     * @return
     */
    public ThreadPoolExecutor buildCommon() {
        return buildCommonExecutor(this);
    }

    /**
     * Build according to dynamic field, and then wrapper with ttl
     * @see com.alibaba.ttl.TransmittableThreadLocal
     * @return
     */
    public ExecutorService buildWrapperWithTtl() {
        if (dynamic) {
            return TtlExecutors.getTtlExecutorService(buildDtpExecutor(this));
        } else {
            return TtlExecutors.getTtlExecutorService(buildCommonExecutor(this));
        }
    }

    private DtpExecutor buildDtpExecutor(ThreadPoolBuilder builder) {
        Assert.notNull(builder.threadPoolName, "The thread pool name must not be null.");
        DtpExecutor dtpExecutor = new DtpExecutor(
                builder.corePoolSize,
                builder.maximumPoolSize,
                builder.keepAliveTime,
                builder.timeUnit,
                builder.workQueue,
                builder.threadFactory,
                builder.rejectedExecutionHandler
        );
        dtpExecutor.allowCoreThreadTimeOut(builder.allowCoreThreadTimeOut);
        dtpExecutor.setThreadPoolName(builder.threadPoolName);
        dtpExecutor.setNotifyItems(notifyItems);
        return dtpExecutor;
    }

    private ThreadPoolExecutor buildCommonExecutor(ThreadPoolBuilder builder) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                builder.corePoolSize,
                builder.maximumPoolSize,
                builder.keepAliveTime,
                builder.timeUnit,
                builder.workQueue,
                builder.threadFactory,
                builder.rejectedExecutionHandler
        );
        executor.allowCoreThreadTimeOut(builder.allowCoreThreadTimeOut);
        return executor;
    }

}
