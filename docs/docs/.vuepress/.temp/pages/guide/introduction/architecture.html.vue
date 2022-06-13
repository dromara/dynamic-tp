<template><div><div class="custom-container tip"><p class="custom-container-title">提示</p>
<p>框架功能大体可以分为以下几个模块</p>
<p>1.配置变更监听模块</p>
<p>2.服务内部线程池管理模块</p>
<p>3.三方组件线程池管理模块</p>
<p>4.监控模块</p>
<p>5.通知告警模块</p>
</div>
<p>代码结构</p>
<p><img src="https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/059c87e5767a40ddbc52d74ef4bdbe6d~tplv-k3u1fbpfcp-watermark.image?" alt="图片.png" loading="lazy"></p>
<div class="custom-container tip"><p class="custom-container-title">提示</p>
<p>1.adapter模块：主要是适配一些第三方组件的线程池管理，目前已经实现的有 SpringBoot 内置的三大web容器（Tomcat、Jetty、Undertow）、Dubbo、RocketMq、Hystrix 的线程池管理，
后续会接入其他常用组件的线程池管理。</p>
<p>2.common模块：主要是一些各个模板都会用到的类，解耦依赖，复用代码，大家日常开发中可能也经常会这样做。</p>
<p>3.core模块：该框架的核心代码都在这个模块里，包括动态调整参数，监控报警，以及串联整个项目流程都在此。</p>
<p>4.example模块：提供一个简单使用示例，方便使用者参照</p>
<p>5.logging模块：用于配置框架内部日志的输出，目前主要用于输出线程池监控指标数据到指定文件</p>
<p>6.starter模块：提供独立功能模块的依赖封装、自动配置等相关。</p>
</div>
<h2 id="配置变更监听模块" tabindex="-1"><a class="header-anchor" href="#配置变更监听模块" aria-hidden="true">#</a> 配置变更监听模块</h2>
<p>1.监听特定配置中心的指定配置文件（已实现 Nacos、Apollo、Zookeeper、Consul），可通过内部提供的SPI接口扩展其他实现</p>
<p>2.解析配置文件内容，内置实现 yml、properties、json 配置文件的解析，可通过内部提供的 SPI 接口扩展其他实现</p>
<p>3.通知线程池管理模块实现参数的刷新</p>
<h2 id="服务内部线程池管理模块" tabindex="-1"><a class="header-anchor" href="#服务内部线程池管理模块" aria-hidden="true">#</a> 服务内部线程池管理模块</h2>
<p>1.服务启动时从配置中心拉取配置，生成线程池实例注册到内部线程池注册中心以及Spring容器中</p>
<p>2.接受配置监听模块的刷新事件，实现线程池参数的刷新</p>
<p>3.代码中通过依赖注入（推荐）或者 DtpRegistry.getExecutor() 方法根据线程池名称来获取线程池实例</p>
<h2 id="三方组件线程池管理" tabindex="-1"><a class="header-anchor" href="#三方组件线程池管理" aria-hidden="true">#</a> 三方组件线程池管理</h2>
<p>1.服务启动获取第三方中间件的线程池，被框架管理起来</p>
<p>2.接受参数刷新、指标收集、通知报警事件，进行相应的处理</p>
<h2 id="监控模块" tabindex="-1"><a class="header-anchor" href="#监控模块" aria-hidden="true">#</a> 监控模块</h2>
<p>实现监控指标采集以及输出，默认提供以下三种方式，也可通过内部提供的 SPI 接口扩展其他实现</p>
<p>1.默认实现 JsonLog 输出到磁盘，可以自己采集解析日志，存储展示</p>
<p>2.MicroMeter采集，引入 MicroMeter 相关依赖，暴露相关端点，采集指标数据，结合 Grafana 做监控大盘</p>
<p>3.暴雷自定义 Endpoint 端点（dynamic-tp），可通过 http 方式实时访问</p>
<h2 id="通知告警模块" tabindex="-1"><a class="header-anchor" href="#通知告警模块" aria-hidden="true">#</a> 通知告警模块：</h2>
<p>对接办公平台，实现通知告警功能，已支持钉钉、企微、飞书，可通过内部提供的 SPI 接口扩展其他实现，通知告警类型如下</p>
<p>1.线程池主要参数变更通知</p>
<p>2.阻塞队列容量达到设置的告警阈值</p>
<p>3.线程池活性达到设置的告警阈值</p>
<p>4.触发拒绝策略告警，格式：A/B，A：该报警项前后两次报警区间累加数量，B：该报警项累计总数</p>
<p>5.任务执行超时告警，格式：A/B，A：该报警项前后两次报警区间累加数量，B：该报警项累计总数</p>
<p>6.任务等待超时告警，格式：A/B，A：该报警项前后两次报警区间累加数量，B：该报警项累计总数</p>
<p><img src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/91ea4c3e1166426e8dca9903dacfd9eb~tplv-k3u1fbpfcp-zoom-1.image" alt="" loading="lazy"></p>
</div></template>
