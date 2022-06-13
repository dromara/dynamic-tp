<template><div><p>你还在为 Dubbo 线程池溢出而烦恼吗？😭😭😭</p>
<p>你还在为 RocketMq 消费积压而烦恼吗？😭😭😭</p>
<p>快快使用 DynamicTp 的三方中间件线程池管理功能吧，完美减少你的烦恼。😅😅😅</p>
<div class="custom-container tip"><p class="custom-container-title">提示</p>
<p>已接入三方中间件</p>
<ol>
<li>
<p>SpringBoot 内置 Tomcat 线程池管理</p>
</li>
<li>
<p>SpringBoot 内置 Jetty 线程池管理</p>
</li>
<li>
<p>SpringBoot 内置 Undertow 线程池管理</p>
</li>
<li>
<p>Dubbo（Apache、Alibaba） 服务提供端线程池管理</p>
</li>
<li>
<p>RocketMq 消费端线程池管理</p>
</li>
<li>
<p>Hystrix 线程池管理</p>
</li>
</ol>
</div>
<p>springboot 内置的三大 webserver 集成包默认会引入，不需要自己引入，其他三方组件的包需要自己引入</p>
<div class="language-xml ext-xml line-numbers-mode"><pre v-pre class="language-xml"><code>   <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>dependency</span><span class="token punctuation">></span></span>
        <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>groupId</span><span class="token punctuation">></span></span>io.github.lyh200<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>groupId</span><span class="token punctuation">></span></span>
        <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>artifactId</span><span class="token punctuation">></span></span>dynamic-tp-spring-boot-starter-adapter-dubbo<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>artifactId</span><span class="token punctuation">></span></span>
        <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>version</span><span class="token punctuation">></span></span>1.0.6<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>version</span><span class="token punctuation">></span></span>
    <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>dependency</span><span class="token punctuation">></span></span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><div class="language-xml ext-xml line-numbers-mode"><pre v-pre class="language-xml"><code>    <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>dependency</span><span class="token punctuation">></span></span>
        <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>groupId</span><span class="token punctuation">></span></span>io.github.lyh200<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>groupId</span><span class="token punctuation">></span></span>
        <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>artifactId</span><span class="token punctuation">></span></span>dynamic-tp-spring-boot-starter-adapter-rocketmq<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>artifactId</span><span class="token punctuation">></span></span>
        <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>version</span><span class="token punctuation">></span></span>1.0.6<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>version</span><span class="token punctuation">></span></span>
    <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>dependency</span><span class="token punctuation">></span></span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><div class="language-xml ext-xml line-numbers-mode"><pre v-pre class="language-xml"><code>    <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>dependency</span><span class="token punctuation">></span></span>
        <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>groupId</span><span class="token punctuation">></span></span>io.github.lyh200<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>groupId</span><span class="token punctuation">></span></span>
        <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>artifactId</span><span class="token punctuation">></span></span>dynamic-tp-spring-boot-starter-adapter-hystrix<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>artifactId</span><span class="token punctuation">></span></span>
        <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>version</span><span class="token punctuation">></span></span>1.0.6<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>version</span><span class="token punctuation">></span></span>
    <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>dependency</span><span class="token punctuation">></span></span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><div class="custom-container tip"><p class="custom-container-title">提示</p>
<p>1.三方组件线程池配置请参考 快速使用 / 配置文件</p>
<p>2.Tomcat、Jetty、Undertow 线程池目前只享有动态调参和监控功能，没通知报警功能</p>
<p>3.Dubbo、RocketMq、Hystrix 享有动态调参、监控、通知告警 完整的功能</p>
<p>4.注意配置时 threadPoolName 规则，配置文件有注释</p>
</div>
</div></template>
