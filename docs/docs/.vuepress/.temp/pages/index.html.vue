<template><div><p align="center">
	<a href="https://gitee.com/dromara/dynamic-tp"><img src="https://gitee.com/dromara/dynamic-tp/badge/star.svg"></a>
  <a href="https://gitee.com/dromara/dynamic-tp/members"><img src="https://gitee.com/dromara/dynamic-tp/badge/fork.svg"></a>
	<a href="https://github.com/dromara/dynamic-tp"><img src="https://img.shields.io/github/stars/dromara/dynamic-tp?style=flat-square&logo=github"></a>
  <a href="https://github.com/dromara/dynamic-tp/network/members"><img src="https://img.shields.io/github/forks/dromara/dynamic-tp?style=flat-square&logo=GitHub"></a>
  <a href="https://github.com/dromara/dynamic-tp/blob/master/LICENSE"><img src="https://img.shields.io/github/license/dromara/dynamic-tp.svg?style=flat-square"></a>
  <a target="_blank" href="https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/530709dc29604630b6d1537d7c160ea5~tplv-k3u1fbpfcp-watermark.image"><img src='https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ddfaed2cce2a47608fb0c0c375a10f08~tplv-k3u1fbpfcp-zoom-1.image' alt='备注加群'></a>
</p>
<h2 id="功能特性-✅" tabindex="-1"><a class="header-anchor" href="#功能特性-✅" aria-hidden="true">#</a> 功能特性 ✅</h2>
<ul>
<li>
<p><strong>代码零侵入</strong>：所有配置都放在配置中心，对业务代码零侵入</p>
</li>
<li>
<p><strong>轻量简单</strong>：基于 springboot 实现，引入 starter，接入只需简单4步就可完成，顺利3分钟搞定</p>
</li>
<li>
<p><strong>高可扩展</strong>：框架核心功能都提供 SPI 接口供用户自定义个性化实现（配置中心、配置文件解析、通知告警、监控数据采集、任务包装等等）</p>
</li>
<li>
<p><strong>线上大规模应用</strong>：参考<a href="https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html" target="_blank" rel="noopener noreferrer">美团线程池实践<ExternalLinkIcon/></a>，美团内部已经有该理论成熟的应用经验</p>
</li>
<li>
<p><strong>通知报警</strong>：提供多种报警维度（配置变更通知、活性报警、容量阈值报警、拒绝触发报警、任务执行或等待超时报警），已支持企业微信、钉钉、飞书报警，同时提供 SPI 接口可自定义扩展实现</p>
</li>
<li>
<p><strong>监控</strong>：定时采集线程池指标数据，支持通过 MicroMeter、JsonLog 日志输出、Endpoint 三种方式，可通过 SPI 接口自定义扩展实现</p>
</li>
<li>
<p><strong>任务增强</strong>：提供任务包装功能，实现TaskWrapper接口即可，如 TtlTaskWrapper 可以支持线程池上下文信息传递，以及给任务设置标识id，方便问题追踪</p>
</li>
<li>
<p><strong>兼容性</strong>：JUC 普通线程池也可以被框架监控，@Bean 定义时加 @DynamicTp 注解即可</p>
</li>
<li>
<p><strong>可靠性</strong>：框架提供的线程池实现 Spring 生命周期方法，可以在 Spring 容器关闭前尽可能多的处理队列中的任务</p>
</li>
<li>
<p><strong>多模式</strong>：参考Tomcat线程池提供了 IO 密集型场景使用的 EagerDtpExecutor 线程池</p>
</li>
<li>
<p><strong>支持多配置中心</strong>：基于主流配置中心实现线程池参数动态调整，实时生效，已支持 Nacos、Apollo、Zookeeper、Consul，同时也提供 SPI 接口可自定义扩展实现</p>
</li>
<li>
<p><strong>中间件线程池管理</strong>：集成管理常用第三方组件的线程池，已集成Tomcat、Jetty、Undertow、Dubbo、RocketMq、Hystrix等组件的线程池管理（调参、监控报警）</p>
</li>
</ul>
</div></template>
