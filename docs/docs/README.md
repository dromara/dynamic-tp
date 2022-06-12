---
home: true
icon: home
title: 首页
heroImage: /logo.png
heroText: dynamic-tp
tagline: 🔥🔥🔥 基于配置中心的轻量级动态线程池
actions:
    - text: 快速上手 🎉
      link: /guide/use/quick-start
      type: primary
    - text: star支持 ❤️
      link: https://gitee.com/dromara/dynamic-tp
    - text: 专栏文章 📚
      link: https://juejin.cn/column/7053801521502224392

features:
  - title: 动态调参
    icon: customize
    details: 在运行时动态调整线程池参数，包括核心线程数、最大线程数、空闲线程超时时间、任务队列大小等
    link: "/guide/use/quick-start"

  - title: 通知报警
    icon: notice
    details: 目前支持调参通知、活性、队列容量、拒绝策略、超时共六类通知报警维度，在运行时实时+定时检测，触发阈值进行推送
    link: "/guide/notice/alarm"

  - title: 运行监控
    icon: eye
    details: 定时采集线程池运行指标数据，支持json long输出、micrometer、endpoint三种指标数据输出方式，可灵活选择
    link: "/guide/monitor/way"

  - title: 三方包集成
    icon: grid
    details: 集成三方中间件线程池管理，已接入dubbo、rocketmq、tomcat、undertow、hystrix、jetty等组件线程池管理
    link: "/guide/middleware/middleware"
---

<p align="center">
	<a href="https://gitee.com/dromara/dynamic-tp"><img src="https://gitee.com/dromara/dynamic-tp/badge/star.svg"></a>
  <a href="https://gitee.com/dromara/dynamic-tp/members"><img src="https://gitee.com/dromara/dynamic-tp/badge/fork.svg"></a>
	<a href="https://github.com/dromara/dynamic-tp"><img src="https://img.shields.io/github/stars/dromara/dynamic-tp?style=flat-square&logo=github"></a>
  <a href="https://github.com/dromara/dynamic-tp/network/members"><img src="https://img.shields.io/github/forks/dromara/dynamic-tp?style=flat-square&logo=GitHub"></a>
  <a href="https://github.com/dromara/dynamic-tp/blob/master/LICENSE"><img src="https://img.shields.io/github/license/dromara/dynamic-tp.svg?style=flat-square"></a>
  <a target="_blank" href="https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/530709dc29604630b6d1537d7c160ea5~tplv-k3u1fbpfcp-watermark.image"><img src='https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ddfaed2cce2a47608fb0c0c375a10f08~tplv-k3u1fbpfcp-zoom-1.image' alt='备注加群'></a>
</p>


## 功能特性 ✅

- **代码零侵入**：所有配置都放在配置中心，对业务代码零侵入

- **轻量简单**：基于 springboot 实现，引入 starter，接入只需简单4步就可完成，顺利3分钟搞定

- **高可扩展**：框架核心功能都提供 SPI 接口供用户自定义个性化实现（配置中心、配置文件解析、通知告警、监控数据采集、任务包装等等）

- **线上大规模应用**：参考[美团线程池实践](https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html)，美团内部已经有该理论成熟的应用经验 

- **通知报警**：提供多种报警维度（配置变更通知、活性报警、容量阈值报警、拒绝触发报警、任务执行或等待超时报警），已支持企业微信、钉钉、飞书报警，同时提供 SPI 接口可自定义扩展实现

- **监控**：定时采集线程池指标数据，支持通过 MicroMeter、JsonLog 日志输出、Endpoint 三种方式，可通过 SPI 接口自定义扩展实现

- **任务增强**：提供任务包装功能，实现TaskWrapper接口即可，如 TtlTaskWrapper 可以支持线程池上下文信息传递，以及给任务设置标识id，方便问题追踪

- **兼容性**：JUC 普通线程池也可以被框架监控，@Bean 定义时加 @DynamicTp 注解即可

- **可靠性**：框架提供的线程池实现 Spring 生命周期方法，可以在 Spring 容器关闭前尽可能多的处理队列中的任务

- **多模式**：参考Tomcat线程池提供了 IO 密集型场景使用的 EagerDtpExecutor 线程池

- **支持多配置中心**：基于主流配置中心实现线程池参数动态调整，实时生效，已支持 Nacos、Apollo、Zookeeper、Consul，同时也提供 SPI 接口可自定义扩展实现

- **中间件线程池管理**：集成管理常用第三方组件的线程池，已集成Tomcat、Jetty、Undertow、Dubbo、RocketMq、Hystrix等组件的线程池管理（调参、监控报警）