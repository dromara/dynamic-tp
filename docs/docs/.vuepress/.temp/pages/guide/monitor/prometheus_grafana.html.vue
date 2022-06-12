<template><div><h2 id="集成步骤" tabindex="-1"><a class="header-anchor" href="#集成步骤" aria-hidden="true">#</a> 集成步骤</h2>
<p>这块要讲的是集成prometheus+grafana做监控，事先你得安装好prometheus+grafana，这个就不展开讲了，网上教程很多，测试使用可以直接用docker安装，非常简单，安装完之后接着往下看。</p>
<p>1.首先配置文件中开启micrometer数据采集</p>
<div class="language-yaml ext-yml line-numbers-mode"><pre v-pre class="language-yaml"><code>   <span class="token key atrule">enabledCollect</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>
   <span class="token key atrule">collectorType</span><span class="token punctuation">:</span> micrometer
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div></div></div><p>2.项目中引入 micrometer-prometheus 依赖，注意可能有版本不兼容问题，我测试遇到过</p>
<div class="language-xml ext-xml line-numbers-mode"><pre v-pre class="language-xml"><code>  <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>dependency</span><span class="token punctuation">></span></span>
      <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>groupId</span><span class="token punctuation">></span></span>io.micrometer<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>groupId</span><span class="token punctuation">></span></span>
      <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>artifactId</span><span class="token punctuation">></span></span>micrometer-registry-prometheus<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>artifactId</span><span class="token punctuation">></span></span>
      <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>version</span><span class="token punctuation">></span></span>1.8.3<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>version</span><span class="token punctuation">></span></span>
  <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>dependency</span><span class="token punctuation">></span></span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><p>3.开启prometheus指标采集端点</p>
<div class="language-yaml ext-yml line-numbers-mode"><pre v-pre class="language-yaml"><code><span class="token key atrule">management</span><span class="token punctuation">:</span>
  <span class="token key atrule">metrics</span><span class="token punctuation">:</span>
    <span class="token key atrule">export</span><span class="token punctuation">:</span>
      <span class="token key atrule">prometheus</span><span class="token punctuation">:</span> 
        <span class="token key atrule">enabled</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>
  <span class="token key atrule">endpoints</span><span class="token punctuation">:</span>
    <span class="token key atrule">web</span><span class="token punctuation">:</span>
      <span class="token key atrule">exposure</span><span class="token punctuation">:</span>
        <span class="token key atrule">include</span><span class="token punctuation">:</span> <span class="token string">'*'</span>   <span class="token comment"># 测试使用，线上不要用*，按需开启</span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><p>4.配置prometheus数据采集job，这块可以去了解下他的SD机制（Service Discovery），也就是自动到注册中心发现服务，看你所用的注册中心支不支持这种方式，<a href="https://prometheus.io/docs/prometheus/latest/configuration/configuration/#scrape_config" target="_blank" rel="noopener noreferrer">可以去官网查看<ExternalLinkIcon/></a>，k8s，ZK，Eureka、Consul等都是支持的。这里使用static_configs方式，简单的指定地址的静态配置</p>
<div class="language-yaml ext-yml line-numbers-mode"><pre v-pre class="language-yaml"><code><span class="token punctuation">-</span> <span class="token key atrule">job_name</span><span class="token punctuation">:</span> <span class="token string">'prometheus'</span>
    <span class="token key atrule">metrics_path</span><span class="token punctuation">:</span> <span class="token string">'/actuator/prometheus'</span>
    <span class="token key atrule">static_configs</span><span class="token punctuation">:</span>
      <span class="token punctuation">-</span> <span class="token key atrule">targets</span><span class="token punctuation">:</span> <span class="token punctuation">[</span><span class="token string">'192.168.2.104:9098'</span><span class="token punctuation">]</span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><p>job配置后prometheus管理台看到如下图所示，说明已经开始正常采集指标配置</p>
<p><img src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/435f0a69790946f8bff7761c40a0a0db~tplv-k3u1fbpfcp-zoom-1.image" alt="采集指标" loading="lazy"></p>
<p>5.然后就是配置grafana数据可视化，配置如下图，需要该panel配置Json的可以加我发你，到这里监控就搭建起来了，其实也很简单，然后就可以实时监控线程池数据指标变动了</p>
<p><img src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a36430c06bf44ca987ff54b500a14172~tplv-k3u1fbpfcp-zoom-1.image" alt="监控数据" loading="lazy"></p>
<div class="custom-container warning"><p class="custom-container-title">注意</p>
<p>完成上述所有步骤后，记得重新修改下每个pannel的数据源，这样才会正确显示监控数据</p>
<p><img src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/39e2c37af1fb48679b5fdd56e7f89c37~tplv-k3u1fbpfcp-watermark.image?" alt="image.png" loading="lazy"></p>
</div>
</div></template>
