---
title: hystrix
icon: plugin
order: 1
author: yanhom
date: 2023-02-11
category:
  - hystrix
tag:
  - hystrix
  - circuitbreaker
  - dynamictp
sticky: true
star: true
---

### 使用步骤

1. 引入下述依赖

```xml
   <dependency>
        <groupId>cn.dynamictp</groupId>
        <artifactId>dynamic-tp-spring-boot-starter-adapter-hystrix</artifactId>
        <version>1.1.0</version>
    </dependency>
```

2. 配置文件中配置 hystrix 线程池

```yaml
spring:
  dynamic:
    tp:
      enabled: true
      enabledCollect: true          # 是否开启监控指标采集，默认false
      collectorTypes: micrometer    # 监控数据采集器类型（logging | micrometer | internal_logging），默认micrometer
      monitorInterval: 5            # 监控时间间隔（报警判断、指标采集），默认5s
      hystrixTp:                                   # hystrix 线程池配置
        - threadPoolName: testThreadPoolKey
          corePoolSize: 100
          maximumPoolSize: 200
          keepAliveTime: 60
```

3. 启动日志

第一次访问时看到有如下日志输出说明接入成功，如果开启了通知，同时会推送参数修改通知

```bash
DynamicTp adapter, hystrix init end, executor ExecutorWrapper(threadPoolName=testThreadPoolKey, executor=java.util.concurrent.ThreadPoolExecutor@19f57a1[Running, pool size = 0, active threads = 0, queued tasks = 0, completed tasks = 0], threadPoolAliasName=null, notifyItems=[NotifyItem(platforms=[wechat, ding, lark, email], enabled=true, type=liveness, threshold=70, interval=120, clusterLimit=1), NotifyItem(platforms=[wechat, ding, lark, email], enabled=true, type=change, threshold=0, interval=1, clusterLimit=1), NotifyItem(platforms=[wechat, ding, lark, email], enabled=true, type=capacity, threshold=70, interval=120, clusterLimit=1)], notifyEnabled=true)
DynamicTp hystrixTp adapter, [testThreadPoolKey] refreshed end, changed keys: [corePoolSize, maxPoolSize], corePoolSize: [5 => 100], maxPoolSize: [5 => 200], keepAliveTime: [60 => 60]
```

::: tip

1. hystrix 线程池在第一次访问时才进行初始化，服务启动并不会立即初始化
2. hystrix 线程池享有动态调参、监控、通知告警完整的功能
3. rocketmq 线程池通知告警项有（调参通知、活性告警、队列容量告警），可通过 notifyItems 自定义配置项值，默认全部开启
:::