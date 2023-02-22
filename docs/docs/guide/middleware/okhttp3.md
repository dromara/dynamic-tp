---
title: okhttp3
icon: plugin
order: 1
author: yanhom
date: 2023-02-11
category:
  - okhttp3
tag:
  - okhttp3
  - httpclient
  - dynamictp
sticky: true
star: true
---

### 使用步骤

1. 引入下述依赖

```xml
   <dependency>
        <groupId>cn.dynamictp</groupId>
        <artifactId>dynamic-tp-spring-boot-starter-adapter-okhttp3</artifactId>
        <version>1.1.0</version>
    </dependency>
```

2. 配置文件中配置 okhttp3 线程池

```yaml
spring:
  dynamic:
    tp:
      enabled: true
      enabledCollect: true          # 是否开启监控指标采集，默认false
      collectorTypes: micrometer    # 监控数据采集器类型（logging | micrometer | internal_logging），默认micrometer
      monitorInterval: 5            # 监控时间间隔（报警判断、指标采集），默认5s
      okhttp3Tp:                                    # okhttp3 线程池配置
        - threadPoolName: okHttpClientTp
          corePoolSize: 100
          maximumPoolSize: 200
          keepAliveTime: 60
```

3. 启动日志

服务启动看到有如下日志输出说明接入成功，如果开启了通知，同时会推送参数修改通知

```bash
DynamicTp adapter, okhttp3 executors init end, executors: {okHttpClientTp=ExecutorWrapper(threadPoolName=okHttpClientTp, executor=java.util.concurrent.ThreadPoolExecutor@f336fd[Running, pool size = 0, active threads = 0, queued tasks = 0, completed tasks = 0], threadPoolAliasName=null, notifyItems=[NotifyItem(platforms=null, enabled=true, type=liveness, threshold=70, interval=120, clusterLimit=1), NotifyItem(platforms=null, enabled=true, type=change, threshold=0, interval=1, clusterLimit=1), NotifyItem(platforms=null, enabled=true, type=capacity, threshold=70, interval=120, clusterLimit=1)], notifyEnabled=true)}
DynamicTp okhttp3Tp adapter, [okHttpClientTp] refreshed end, changed keys: [corePoolSize, maxPoolSize], corePoolSize: [0 => 100], maxPoolSize: [2147483647 => 200], keepAliveTime: [60 => 60]
```

::: tip

1. 服务启动会自动从 Spring 容器中获取所有被 Spring 容器管理的 OkHttpClient 实例 
2. 线程池名称规则：beanName + Tp（可以在启动日志找输出的线程池名称）
3. okhttp3 线程池只在异步请求时生效，同步请求不会使用 okhttp3 线程池
4. okhttp3 线程池享有动态调参、监控、通知告警完整的功能
5. okhttp3 线程池通知告警项有（调参通知、活性告警、队列容量告警），可通过 notifyItems 自定义配置项值，默认全部开启
:::