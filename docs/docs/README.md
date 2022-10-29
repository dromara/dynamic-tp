---
home: true
icon: home
title: 首页
heroImage: /logo.png
heroText: dynamic-tp
tagline: 🔥🔥🔥 基于配置中心的轻量级动态可监控线程池
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
    details: 定时采集线程池运行指标数据，提供jsonlog、micrometer、endpoint三种指标数据采集方式，可灵活选择
    link: "/guide/monitor/way"

  - title: 三方包集成
    icon: grid
    details: 集成三方中间件线程池管理，已接入dubbo、rocketmq、hystrix、tomcat、undertow、jetty等组件线程池管理
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


# 功能特性 ✅

- **代码零侵入**：所有配置都放在配置中心，对业务代码零侵入

- **轻量简单**：基于 springboot 实现，引入 starter，接入只需简单4步就可完成，顺利3分钟搞定

- **高可扩展**：框架核心功能都提供 SPI 接口供用户自定义个性化实现（配置中心、配置文件解析、通知告警、监控数据采集、任务包装等等）

- **线上大规模应用**：参考[美团线程池实践](https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html)，美团内部已经有该理论成熟的应用经验

- **多平台通知报警**：提供多种报警维度（配置变更通知、活性报警、容量阈值报警、拒绝触发报警、任务执行或等待超时报警），已支持企业微信、钉钉、飞书、邮件报警，同时提供 SPI 接口可自定义扩展实现

- **监控**：定时采集线程池指标数据，支持通过 MicroMeter、JsonLog 日志输出、Endpoint 三种方式，可通过 SPI 接口自定义扩展实现

- **任务增强**：提供任务包装功能，实现TaskWrapper接口即可，如 MdcTaskWrapper、TtlTaskWrapper、SwTraceTaskWrapper，可以支持线程池上下文信息传递

- **兼容性**：JUC 普通线程池和 Spring 中的 ThreadPoolTaskExecutor 也可以被框架监控，@Bean 定义时加 @DynamicTp 注解即可

- **可靠性**：框架提供的线程池实现 Spring 生命周期方法，可以在 Spring 容器关闭前尽可能多的处理队列中的任务

- **多模式**：参考Tomcat线程池提供了 IO 密集型场景使用的 EagerDtpExecutor 线程池

- **支持多配置中心**：基于主流配置中心实现线程池参数动态调整，实时生效，已支持 Nacos、Apollo、Zookeeper、Consul、Etcd，同时也提供 SPI 接口可自定义扩展实现

- **中间件线程池管理**：集成管理常用第三方组件的线程池，已集成Tomcat、Jetty、Undertow、Dubbo、RocketMq、Hystrix、Grpc 等组件的线程池管理（调参、监控报警）


# 技术架构 

![技术架构](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/38e4bf71d2c84b7ba67d7059b5432a7e~tplv-k3u1fbpfcp-zoom-1.image)

# Star History

[![Star History Chart](https://api.star-history.com/svg?repos=dromara/dynamic-tp&type=Date)](https://star-history.com/#dromara/dynamic-tp&Date)

# 知识星球

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/894a4e918ff14c13b4a66d3f30f7ff7e~tplv-k3u1fbpfcp-zoom-1.image)

# Dromara 成员项目

<div>
    <div class="com-box-f s-width">
        <div class="com-box com-box-you">
            <a href="https://gitee.com/dromara/TLog" target="_blank">
                <img src="/images/tlog-logo.png" msg="一个轻量级的分布式日志标记追踪神器，10分钟即可接入，自动对日志打标签完成微服务的链路追踪">
            </a>
            <a href="https://gitee.com/dromara/liteFlow" target="_blank">
                <img src="/images/lite-flow.png" msg="轻量，快速，稳定，可编排的组件式流程引擎">
            </a>
            <a href="https://hutool.cn/" target="_blank">
                <img src="/images/hutool-logo.png" msg="🍬小而全的Java工具类库，使Java拥有函数式语言般的优雅，让Java语言也可以“甜甜的”。">
            </a>
            <a href="https://sa-token.dev33.cn/" target="_blank">
                <img src="/images/sa-token.png" msg="一个轻量级 java 权限认证框架，让鉴权变得简单、优雅！">
            </a>
            <a href="https://gitee.com/dromara/hmily" target="_blank">
                <img src="/images/hmily.png" msg="高性能一站式分布式事务解决方案。">
            </a>
            <a href="https://gitee.com/dromara/Raincat" target="_blank">
                <img src="/images/raincat.png" msg="强一致性分布式事务解决方案。">
            </a>
            <a href="https://gitee.com/dromara/myth" target="_blank">
                <img src="/images/myth.png" msg="可靠消息分布式事务解决方案。">
            </a>
            <a href="https://cubic.jiagoujishu.com/" target="_blank">
                <img src="/images/cubic-logo.png" msg="一站式问题定位平台，以agent的方式无侵入接入应用，完整集成arthas功能模块，致力于应用级监控，帮助开发人员快速定位问题">
            </a>
            <a href="https://maxkey.top/" target="_blank">
                <img src="/images/maxkey-logo.png" msg="业界领先的身份管理和认证产品">
            </a>
            <a href="http://forest.dtflyx.com/" target="_blank">
                <img src="/images/forest-logo.png" msg="Forest能够帮助您使用更简单的方式编写Java的HTTP客户端" nf>
            </a>
            <a href="https://jpom.io/" target="_blank">
                <img src="/images/jpom-logo.png" msg="一款简而轻的低侵入式在线构建、自动部署、日常运维、项目监控软件">
            </a>
            <a href="https://su.usthe.com/" target="_blank">
                <img src="/images/sureness.png" msg="面向 REST API 的高性能认证鉴权框架">
            </a>
            <a href="https://easy-es.cn/" target="_blank">
                <img src="/images/easy-es2.png" msg="🚀傻瓜级ElasticSearch搜索引擎ORM框架">
            </a>
            <a href="https://gitee.com/dromara/northstar" target="_blank">
                <img src="/images/northstar_logo.png" msg="Northstar盈富量化交易平台">
            </a>
            <a href="https://hertzbeat.com/" target="_blank">
                <img src="/images/hertzbeat-logo.png" msg="易用友好的云监控系统">
            </a>
            <a href="https://dromara.gitee.io/fast-request/" target="_blank">
                <img src="/images/fast-request.png" msg="Idea 版 Postman，为简化调试API而生">
            </a>
            <a href="https://www.jeesuite.com/" target="_blank">
                <img src="/images/mendmix.png" msg="开源分布式云原生架构一站式解决方案">
            </a>
            <a href="https://gitee.com/dromara/koalas-rpc" target="_blank">
                <img src="/images/koalas-rpc2.png" msg="企业生产级百亿日PV高可用可拓展的RPC框架。">
            </a>
            <a href="https://async.sizegang.cn/" target="_blank">
                <img src="/images/gobrs-async.png" msg="🔥 配置极简功能强大的异步任务动态编排框架">
            </a>
            <a href="https://dynamictp.cn/" target="_blank">
                <img src="/images/dynamic-tp.png" msg="🔥🔥🔥 基于配置中心的轻量级动态可监控线程池">
            </a>
            <a href="https://www.x-easypdf.cn" target="_blank">
                <img src="/images/easypdf.png" msg="一个用搭积木的方式构建pdf的框架（基于pdfbox）">
            </a>
            <a href="http://dromara.gitee.io/image-combiner" target="_blank">
                <img src="/images/imageCombiner.png" msg="一个专门用于图片合成的工具，没有很复杂的功能，简单实用，却不失强大">
            </a>
            <a href="https://www.herodotus.cn/" target="_blank">
				<img src="/images/dantecloud.png" msg="Dante-Cloud 是一款企业级微服务架构和服务能力开发平台。">
            </a>
            <a href="https://www.mtruning.club/" target="_blank">
				<img src="/images/go-view.png" msg="GoView 是一个高效的拖拽式低代码数据可视化开发平台。">
            </a>
        </div>
        <div style="height: 10px; clear: both;"></div>
        <p>
            为往圣继绝学，一个人或许能走的更快，但一群人会走的更远。
        </p>
    </div>
    <div style="height: 60px;"></div>
</div>

<link rel="stylesheet" href="/index.css">