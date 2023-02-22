---
title: jetty 线程池管理
icon: alias
order: 1
author: yanhom
date: 2023-02-11
category:
  - jetty
tag:
  - jetty
  - webserver
  - dynamictp
sticky: true
star: true
---

### 使用步骤

1. 引入下述依赖

```xml
   <dependency>
        <groupId>cn.dynamictp</groupId>
        <artifactId>dynamic-tp-spring-boot-starter-adapter-webserver</artifactId>
        <version>1.1.0</version>
    </dependency>
```

2. 配置文件中配置 jetty 线程池

```yaml
spring:
  dynamic:
    tp:
      enabled: true
      enabledCollect: true          # 是否开启监控指标采集，默认false
      collectorTypes: micrometer    # 监控数据采集器类型（logging | micrometer | internal_logging），默认micrometer
      monitorInterval: 5            # 监控时间间隔（报警判断、指标采集），默认5s
      jettyTp:                                     # jetty weberver线程池配置
        corePoolSize: 100
        maximumPoolSize: 200
```

3. 启动日志

服务启动看到有如下日志输出说明接入成功，如果开启了通知，同时会推送参数修改通知

```bash
DynamicTp adapter, web server executor init end, executor: QueuedThreadPool[qtp32153965]@1eaa16d{STARTED,8<=10<=200,i=0,r=-1,q=0}[ReservedThreadExecutor@12e242d{reserved=0/16,pending=0}]     
DynamicTp adapter [jettyTp] refreshed end, corePoolSize: [10 => 100], maxPoolSize: [200 => 200]
```

::: tip

1. 线程池名称：jettyTp
2. jettyTp 线程池目前只享有动态调参和监控功能，没通知报警功能
:::