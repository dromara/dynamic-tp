---
title: 发版记录
icon: note
order: 1
author: yanhom
date: 2022-06-11
category:
  - 发版记录
sticky: false
star: true
---


::: tip

## v1.0.5

### Features

+ logging模块添加log4j2支持

+ 配置文件支持json格式，zk已支持json、properties格式配置


### BugFix

+ [#I54B4R](https://gitee.com/dromara/dynamic-tp/issues/I54B4R)  


### Refactor

+ 部分代码优化

### Dependency

+ transmittable-thread-local升级到2.12.6

+ micrometer升级到1.8.5

:::

::: tip

## v1.0.4

### Features

+ 配置中心支持Consul

+ 监控告警模块增加任务排队等候超时、任务执行超时监控告警

+ 线程池完全配置在配置中心，无需代码编程式配置，服务启动会自动创建线程池实例，交给Spring容器管理

+ 拒绝策略告警优化，支持前后告警间隔计数

+ 相关代码优化

:::

::: tip

## v1.0.3

### Features

+ 配置中心支持Zookeeper

+ 线程池交由Spring管理其生命周期，可以通过依赖注入方式使用

+ 创建时添加@DynamicTp注解支持监控JUC原生线程池

+ 仿照Tomcat线程池设计，提供IO密集型线程池（EagerDtpExecutor）

+ 相关代码优化，增加必要校验

:::

::: tip

## v1.0.2

### Features

+ 配置中心支持Nacos、Apollo、Zookeeper

+ 告警平台支持企微、钉钉

+ 监控指标数据采集支持json日志输出、MicorMeter以及Endpoint三种方式

+ 第三方组件线程池管理已集成SpringBoot内置三大WebServer（Tomcat、Jetty、Undertow）

+ 核心模块都提供SPI接口可供自定义扩展（配置中心、配置文件解析、告警平台、监控指标数据采集）

+ 提供完整使用示例（包含Grafana配置面板Json文件，直接import即可使用）

:::