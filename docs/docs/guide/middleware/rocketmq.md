---
title: rocketmq
icon: plugin
order: 1
author: yanhom
date: 2023-02-11
category:
  - rocketmq
tag:
  - rocketmq
  - mq
  - dynamictp
sticky: true
star: true
---

你还在为 RocketMq 消费积压而烦恼吗？😭😭😭

快快使用 DynamicTp 的三方中间件线程池管理功能吧，一定程度上能减少你的烦恼。😅😅😅

### 使用步骤

1. 引入下述依赖

```xml
   <dependency>
        <groupId>cn.dynamictp</groupId>
        <artifactId>dynamic-tp-spring-boot-starter-adapter-rocketmq</artifactId>
        <version>1.1.0</version>
    </dependency>
```

2. 配置文件中配置 rocketmq 线程池

```yaml
spring:
  dynamic:
    tp:
      enabled: true
      enabledCollect: true          # 是否开启监控指标采集，默认false
      collectorTypes: micrometer    # 监控数据采集器类型（logging | micrometer | internal_logging），默认micrometer
      monitorInterval: 5            # 监控时间间隔（报警判断、指标采集），默认5s
      rocketMqTp:                                  # rocketmq 线程池配置
        - threadPoolName: group1#topic1            # 名称规则：group + "#" + topic
          corePoolSize: 200
          maximumPoolSize: 200
          keepAliveTime: 60
```

3. 启动日志

服务启动看到有如下日志输出说明接入成功，如果开启了通知，同时会推送参数修改通知

```bash
DynamicTp adapter, rocketMq consumer executors init end, executors: {group#topic=ExecutorWrapper(threadPoolName=group#topic, executor=java.util.concurrent.ThreadPoolExecutor@1acd1f1[Running, pool size = 0, active threads = 0, queued tasks = 0, completed tasks = 0], threadPoolAliasName=null, notifyItems=[NotifyItem(platforms=null, enabled=true, type=liveness, threshold=70, interval=120, clusterLimit=1), NotifyItem(platforms=null, enabled=true, type=change, threshold=0, interval=1, clusterLimit=1), NotifyItem(platforms=null, enabled=true, type=capacity, threshold=70, interval=120, clusterLimit=1)], notifyEnabled=true)}
DynamicTp rocketMqTp adapter, [group#topic] refreshed end, changed keys: [corePoolSize, maxPoolSize], corePoolSize: [20 => 200], maxPoolSize: [20 => 200], keepAliveTime: [60 => 60]
```

::: tip

1. 线程池名称规则：group + "#" + topic（可以在启动日志找输出的线程池名称）
2. rocketmq 线程池享有动态调参、监控、通知告警完整的功能
3. rocketmq 线程池通知告警项有（调参通知、活性告警、队列容量告警），可通过 notifyItems 自定义配置项值，默认全部开启
4. 只支持消费端线程池管理
:::