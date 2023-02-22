---
title: tomcat 线程池管理
icon: Apache
order: 1
author: yanhom
date: 2023-02-11
category:
  - tomcat
tag:
  - tomcat
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

2. 配置文件中配置 tomcat 线程池

```yaml
spring:
  dynamic:
    tp:
      enabled: true
      enabledCollect: true          # 是否开启监控指标采集，默认false
      collectorTypes: micrometer    # 监控数据采集器类型（logging | micrometer | internal_logging），默认micrometer
      monitorInterval: 5            # 监控时间间隔（报警判断、指标采集），默认5s
      tomcatTp:                                    # tomcat webserver 线程池配置
        corePoolSize: 100
        maximumPoolSize: 200
        keepAliveTime: 60
```

3. 启动日志

服务启动看到有如下日志输出说明接入成功，如果开启了通知，同时会推送参数修改通知

```bash
DynamicTp adapter, web server executor init end, executor: org.apache.tomcat.util.threads.ThreadPoolExecutor@114579e[Running, pool size = 0, active threads = 0, queued tasks = 0, completed tasks = 0]
DynamicTp adapter [tomcatTp] refreshed end, corePoolSize: [10 => 100], maxPoolSize: [200 => 200], keepAliveTime: [60 => 60]
```

::: tip

1. 线程池名称：tomcatTp
2. tomcat 线程池目前只享有动态调参和监控功能，没通知报警功能
3. tomcat 线程池并没用 juc 线程池，自己维护了一个 ThreadPoolExecutor
:::