---
title: 已集成框架
icon: plugin
order: 1
author: yanhom
date: 2022-06-11
category:
  - 已集成框架
tag:
  - 已集成框架
sticky: true
star: true
---

你还在为 Dubbo 线程池耗尽而烦恼吗？😭😭😭

你还在为 RocketMq 消费积压而烦恼吗？😭😭😭

快快使用 DynamicTp 的三方中间件线程池管理功能吧，一定程度上能减少你的烦恼。😅😅😅

::: tip
已接入三方中间件
1. SpringBoot 内置 Tomcat 线程池管理

2. SpringBoot 内置 Jetty 线程池管理

3. SpringBoot 内置 Undertow 线程池管理

4. Dubbo（Apache、Alibaba） 服务提供端线程池管理

5. RocketMq 消费端线程池管理

6. Hystrix 线程池管理
:::

依赖如下，使用时需要手动引入对应依赖

```xml
   <dependency>
        <groupId>cn.dynamictp</groupId>
        <artifactId>dynamic-tp-spring-boot-starter-adapter-webserver</artifactId>
        <version>1.0.7</version>
    </dependency>
```

```xml
   <dependency>
        <groupId>cn.dynamictp</groupId>
        <artifactId>dynamic-tp-spring-boot-starter-adapter-dubbo</artifactId>
        <version>1.0.7</version>
    </dependency>
```

```xml
    <dependency>
        <groupId>cn.dynamictp</groupId>
        <artifactId>dynamic-tp-spring-boot-starter-adapter-rocketmq</artifactId>
        <version>1.0.7</version>
    </dependency>
```

```xml
    <dependency>
        <groupId>cn.dynamictp</groupId>
        <artifactId>dynamic-tp-spring-boot-starter-adapter-hystrix</artifactId>
        <version>1.0.7</version>
    </dependency>
```

::: tip
1.三方组件线程池配置请参考 快速使用 / 配置文件

2.Tomcat、Jetty、Undertow 线程池目前只享有动态调参和监控功能，没通知报警功能

3.Dubbo、RocketMq、Hystrix 享有动态调参、监控、通知告警 完整的功能

4.注意配置时 threadPoolName 规则，配置文件有注释

5.三方组件线程池通知告警项有（调参通知、活性告警、队列容量告警），可通过 notifyItems 自定义配置项值，默认全部开启
:::