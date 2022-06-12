<template><div><p><strong>使用 ThreadPoolExecutor 过程中你是否有以下痛点呢？</strong></p>
<blockquote>
<p>1.代码中创建了一个 ThreadPoolExecutor，但是不知道那几个核心参数设置多少比较合适</p>
<p>2.凭经验设置参数值，上线后发现需要调整，改代码重启服务，非常麻烦</p>
<p>3.线程池相对开发人员来说是个黑盒，运行情况不能感知到，直到出现问题</p>
</blockquote>
<p>如果你有以上痛点，动态可监控线程池（DynamicTp）或许能帮助到你。</p>
<p>如果看过 ThreadPoolExecutor 的源码，大概可以知道其实它有提供一些 set 方法，可以在运行时动态去修改相应的值，这些方法有：</p>
<div class="language-java ext-java line-numbers-mode"><pre v-pre class="language-java"><code><span class="token keyword">public</span> <span class="token keyword">void</span> <span class="token function">setCorePoolSize</span><span class="token punctuation">(</span><span class="token keyword">int</span> corePoolSize<span class="token punctuation">)</span><span class="token punctuation">;</span>
<span class="token keyword">public</span> <span class="token keyword">void</span> <span class="token function">setMaximumPoolSize</span><span class="token punctuation">(</span><span class="token keyword">int</span> maximumPoolSize<span class="token punctuation">)</span><span class="token punctuation">;</span>
<span class="token keyword">public</span> <span class="token keyword">void</span> <span class="token function">setKeepAliveTime</span><span class="token punctuation">(</span><span class="token keyword">long</span> time<span class="token punctuation">,</span> <span class="token class-name">TimeUnit</span> unit<span class="token punctuation">)</span><span class="token punctuation">;</span>
<span class="token keyword">public</span> <span class="token keyword">void</span> <span class="token function">setThreadFactory</span><span class="token punctuation">(</span><span class="token class-name">ThreadFactory</span> threadFactory<span class="token punctuation">)</span><span class="token punctuation">;</span>
<span class="token keyword">public</span> <span class="token keyword">void</span> <span class="token function">setRejectedExecutionHandler</span><span class="token punctuation">(</span><span class="token class-name">RejectedExecutionHandler</span> handler<span class="token punctuation">)</span><span class="token punctuation">;</span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><p>现在大多数的互联网项目其实都会微服务化部署，有一套自己的服务治理体系，微服务组件中的分布式配置中心扮演的就是动态修改配置，
实时生效的角色。那么我们是否可以结合配置中心来做运行时线程池参数的动态调整呢？答案是肯定的，而且配置中心相对都是高可用的，
使用它也不用过于担心配置推送出现问题这类事儿，而且也能减少研发动态线程池组件本身的难度和工作量。</p>
<p><strong>综上，可以总结出以下的背景</strong></p>
<ul>
<li>
<p>广泛性：在 Java 开发中，想要提高系统性能，线程池已经是一个 90%以上的人都会选择使用的基础工具</p>
</li>
<li>
<p>不确定性：项目中可能会创建很多线程池，既有 IO 密集型的，也有 CPU 密集型的，但线程池的参数并不好确定；需要有套机制在运行过程中动态去调整参数</p>
</li>
<li>
<p>无感知性，线程池运行过程中的各项指标一般感知不到；需要有套监控报警机制在事前、事中就能让开发人员感知到线程池的运行状况，及时处理</p>
</li>
<li>
<p>高可用性，配置变更需要及时推送到客户端；需要有高可用的配置管理推送服务，配置中心是现在大多数互联网系统都会使用的组件，与之结合可以极大提高系统可用性</p>
</li>
</ul>
</div></template>
