<template><div><div class="custom-container tip"><p class="custom-container-title">提示</p>
<h2 id="v1-0-5" tabindex="-1"><a class="header-anchor" href="#v1-0-5" aria-hidden="true">#</a> v1.0.5</h2>
<h3 id="features" tabindex="-1"><a class="header-anchor" href="#features" aria-hidden="true">#</a> Features</h3>
<ul>
<li>
<p>logging模块添加log4j2支持</p>
</li>
<li>
<p>配置文件支持json格式，zk已支持json、properties格式配置</p>
</li>
</ul>
<h3 id="bugfix" tabindex="-1"><a class="header-anchor" href="#bugfix" aria-hidden="true">#</a> BugFix</h3>
<ul>
<li><a href="https://gitee.com/dromara/dynamic-tp/issues/I54B4R" target="_blank" rel="noopener noreferrer">#I54B4R<ExternalLinkIcon/></a></li>
</ul>
<h3 id="refactor" tabindex="-1"><a class="header-anchor" href="#refactor" aria-hidden="true">#</a> Refactor</h3>
<ul>
<li>部分代码优化</li>
</ul>
<h3 id="dependency" tabindex="-1"><a class="header-anchor" href="#dependency" aria-hidden="true">#</a> Dependency</h3>
<ul>
<li>
<p>transmittable-thread-local升级到2.12.6</p>
</li>
<li>
<p>micrometer升级到1.8.5</p>
</li>
</ul>
</div>
<div class="custom-container tip"><p class="custom-container-title">提示</p>
<h2 id="v1-0-4" tabindex="-1"><a class="header-anchor" href="#v1-0-4" aria-hidden="true">#</a> v1.0.4</h2>
<h3 id="features-1" tabindex="-1"><a class="header-anchor" href="#features-1" aria-hidden="true">#</a> Features</h3>
<ul>
<li>
<p>配置中心支持Consul</p>
</li>
<li>
<p>监控告警模块增加任务排队等候超时、任务执行超时监控告警</p>
</li>
<li>
<p>线程池完全配置在配置中心，无需代码编程式配置，服务启动会自动创建线程池实例，交给Spring容器管理</p>
</li>
<li>
<p>拒绝策略告警优化，支持前后告警间隔计数</p>
</li>
<li>
<p>相关代码优化</p>
</li>
</ul>
</div>
<div class="custom-container tip"><p class="custom-container-title">提示</p>
<h2 id="v1-0-3" tabindex="-1"><a class="header-anchor" href="#v1-0-3" aria-hidden="true">#</a> v1.0.3</h2>
<h3 id="features-2" tabindex="-1"><a class="header-anchor" href="#features-2" aria-hidden="true">#</a> Features</h3>
<ul>
<li>
<p>配置中心支持Zookeeper</p>
</li>
<li>
<p>线程池交由Spring管理其生命周期，可以通过依赖注入方式使用</p>
</li>
<li>
<p>创建时添加@DynamicTp注解支持监控JUC原生线程池</p>
</li>
<li>
<p>仿照Tomcat线程池设计，提供IO密集型线程池（EagerDtpExecutor）</p>
</li>
<li>
<p>相关代码优化，增加必要校验</p>
</li>
</ul>
</div>
<div class="custom-container tip"><p class="custom-container-title">提示</p>
<h2 id="v1-0-2" tabindex="-1"><a class="header-anchor" href="#v1-0-2" aria-hidden="true">#</a> v1.0.2</h2>
<h3 id="features-3" tabindex="-1"><a class="header-anchor" href="#features-3" aria-hidden="true">#</a> Features</h3>
<ul>
<li>
<p>配置中心支持Nacos、Apollo、Zookeeper</p>
</li>
<li>
<p>告警平台支持企微、钉钉</p>
</li>
<li>
<p>监控指标数据采集支持json日志输出、MicorMeter以及Endpoint三种方式</p>
</li>
<li>
<p>第三方组件线程池管理已集成SpringBoot内置三大WebServer（Tomcat、Jetty、Undertow）</p>
</li>
<li>
<p>核心模块都提供SPI接口可供自定义扩展（配置中心、配置文件解析、告警平台、监控指标数据采集）</p>
</li>
<li>
<p>提供完整使用示例（包含Grafana配置面板Json文件，直接import即可使用）</p>
</li>
</ul>
</div>
</div></template>
