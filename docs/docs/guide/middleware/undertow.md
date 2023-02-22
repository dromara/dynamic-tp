---
title: undertow
icon: plugin
order: 1
author: yanhom
date: 2023-02-11
category:
  - undertow
tag:
  - undertow
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

2. 配置文件中配置 undertow 线程池

```yaml
spring:
  dynamic:
    tp:
      enabled: true
      enabledCollect: true          # 是否开启监控指标采集，默认false
      collectorTypes: micrometer    # 监控数据采集器类型（logging | micrometer | internal_logging），默认micrometer
      monitorInterval: 5            # 监控时间间隔（报警判断、指标采集），默认5s
      undertowTp:                                  # undertow webserver线程池配置
        corePoolSize: 100
        maximumPoolSize: 200
        keepAliveTime: 60
```

3. 启动日志

第一次访问时看到有如下日志输出说明接入成功，如果开启了通知，同时会推送参数修改通知

```bash
DynamicTp adapter, web server executor init end, executor: org.xnio.nio.NioXnioWorker@17ce31c   
DynamicTp adapter [undertowTp] refreshed end, corePoolSize: [10 => 100], maxPoolSize: [200 => 200], keepAliveTime: [60 => 60]
```

::: tip

1. 线程池名称：undertowTp
2. undertow 线程池目前只享有动态调参和监控功能，没通知报警功能
3. undertow 内部使用 xnio 网络框架，类似 netty
:::