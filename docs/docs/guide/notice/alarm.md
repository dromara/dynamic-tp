---
title: 报警
icon: warn
order: 1
author: yanhom
date: 2022-06-11
category:
  - 报警
tag:
  - 报警
sticky: true
star: true
---

::: tip 

框架目前提供以下告警功能，每一个告警项都可以独立配置是否开启、告警阈值、告警间隔时间、平台等，具体代码请看 core 模块 notify 包，
告警信息同时会高亮与该项相关的字段。

1.核心参数变更通知

2.线程池活跃度告警

3.队列容量告警

4.拒绝策略告警

5.任务执行超时告警

6.任务排队超时告警

:::

## 线程池活跃度告警

活跃度 = activeCount / maximumPoolSize

服务启动后会开启一个定时监控任务，每隔一定时间（可配置）去计算线程池的活跃度，达到配置的 threshold 阈值后会触发一次告警，告警间隔内多次触发不会发送告警通知

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/675f7b2732ba46ae9a0539ec69698c6b~tplv-k3u1fbpfcp-zoom-1.image)


## 队列容量告警

容量使用率 = queueSize / queueCapacity

服务启动后会开启一个定时监控任务，每隔一定时间去计算任务队列的使用率，达到配置的 threshold 阈值后会触发一次告警，告警间隔内多次触发不会发送告警通知

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d65151e3e9ca460eac18f30ea6be05d3~tplv-k3u1fbpfcp-zoom-1.image)


## 拒绝策略告警

```java
/**
 * Do sth before reject.
 * @param executor ThreadPoolExecutor instance
 */
default void beforeReject(ThreadPoolExecutor executor) {
    if (executor instanceof DtpExecutor) {
        DtpExecutor dtpExecutor = (DtpExecutor) executor;
        dtpExecutor.incRejectCount(1);
        Runnable runnable = () -> AlarmManager.doAlarm(dtpExecutor, REJECT);
        AlarmManager.triggerAlarm(dtpExecutor.getThreadPoolName(), REJECT.getValue(), runnable);
    }
}
```

线程池线程数达到配置的最大线程数，且任务队列已满，再提交任务会触发拒绝策略。DtpExecutor 线程池用到的 RejectedExecutionHandler 是经过动态代理包装过的，在执行具体的拒绝策略之前会执行RejectedAware类beforeReject()方法，此方法会去做拒绝数量累加（总数值累加、周期值累加）。且判断如果周期累计值达到配置的阈值，则会触发一次告警通知（同时重置周期累加值为0及上次告警时间为当前时间），告警间隔内多次触发不会发送告警通知

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/651049fe286f4cb099ab8936bfc4b425~tplv-k3u1fbpfcp-zoom-1.image)


## 任务队列超时告警

重写 ThreadPoolExecutor 的 execute() 方法和 beforeExecute() 方法，如果配置了执行超时或排队超时值，则会用DtpRunnable包装任务，同时记录任务的提交时间submitTime，beforeExecute根据当前时间和submitTime的差值就可以计算到该任务在队列中的等待时间，然后判断如果差值大于配置的queueTimeout则累加排队超时任务数量（总数值累加、周期值累加）。且判断如果周期累计值达到配置的阈值，则会触发一次告警通知（同时重置周期累加值为0及上次告警时间为当前时间），告警间隔内多次触发不会发送告警通知

```java
@Override
public void execute(Runnable command) {
    String taskName = null;
    if (command instanceof NamedRunnable) {
        taskName = ((NamedRunnable) command).getName();
    }

    if (CollUtil.isNotEmpty(taskWrappers)) {
        for (TaskWrapper t : taskWrappers) {
            command = t.wrap(command);
        }
    }

    if (runTimeout > 0 || queueTimeout > 0) {
        command = new DtpRunnable(command, taskName);
    }
    super.execute(command);
}
```

```java
@Override
protected void beforeExecute(Thread t, Runnable r) {
    if (!(r instanceof DtpRunnable)) {
        super.beforeExecute(t, r);
        return;
    }
    DtpRunnable runnable = (DtpRunnable) r;
    long currTime = System.currentTimeMillis();
    if (runTimeout > 0) {
        runnable.setStartTime(currTime);
    }
    if (queueTimeout > 0) {
        long waitTime = currTime - runnable.getSubmitTime();
        if (waitTime > queueTimeout) {
            queueTimeoutCount.incrementAndGet();
            Runnable alarmTask = () -> AlarmManager.doAlarm(this, QUEUE_TIMEOUT);
            AlarmManager.triggerAlarm(this.getThreadPoolName(), QUEUE_TIMEOUT.getValue(), alarmTask);
            if (StringUtils.isNotBlank(runnable.getTaskName())) {
                log.warn("DynamicTp execute, queue timeout, poolName: {}, taskName: {}, waitTime: {}ms",
                        this.getThreadPoolName(), runnable.getTaskName(), waitTime);
            }
        }
    }

    super.beforeExecute(t, r);
}
```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a8f34edbedee4683a9525a6e9423a1be~tplv-k3u1fbpfcp-zoom-1.image)



##  任务执行超时告警

重写ThreadPoolExecutor的afterExecute()方法，根据当前时间和beforeExecute()中设置的startTime的差值即可算出任务的实际执行时间，然后判断如果差值大于配置的runTimeout则累加排队超时任务数量（总数值累加、周期值累加）。且判断如果周期累计值达到配置的阈值，则会触发一次告警通知（同时重置周期累加值为0及上次告警时间为当前时间），告警间隔内多次触发不会发送告警通知


```java
@Override
protected void afterExecute(Runnable r, Throwable t) {

    if (runTimeout > 0) {
        DtpRunnable runnable = (DtpRunnable) r;
        long runTime = System.currentTimeMillis() - runnable.getStartTime();
        if (runTime > runTimeout) {
            runTimeoutCount.incrementAndGet();
            Runnable alarmTask = () -> AlarmManager.doAlarm(this, RUN_TIMEOUT);
            AlarmManager.triggerAlarm(this.getThreadPoolName(), RUN_TIMEOUT.getValue(), alarmTask);
            if (StringUtils.isNotBlank(runnable.getTaskName())) {
                log.warn("DynamicTp execute, run timeout, poolName: {}, taskName: {}, runTime: {}ms",
                        this.getThreadPoolName(), runnable.getTaskName(), runTime);
            }
        }
    }

    super.afterExecute(r, t);
}
```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b360e0a129e4413b962b40f6ef415af2~tplv-k3u1fbpfcp-zoom-1.image)


