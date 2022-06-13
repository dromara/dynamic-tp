<template><div><ul>
<li>
<p>线程池实例定义</p>
<p>建议直接配置在配置中心，但是如果想后期再添加到配置中心，可以先用@Bean 编码式声明（方便spring依赖注入）</p>
<div class="language-java ext-java line-numbers-mode"><pre v-pre class="language-java"><code><span class="token annotation punctuation">@Configuration</span>
<span class="token keyword">public</span> <span class="token keyword">class</span> <span class="token class-name">DtpConfig</span> <span class="token punctuation">{</span>  
  
  <span class="token doc-comment comment">/**
   * 通过<span class="token punctuation">{</span><span class="token keyword">@link</span> <span class="token reference"><span class="token class-name">DynamicTp</span></span><span class="token punctuation">}</span> 注解定义普通juc线程池，会享受到该框架监控功能，注解名称优先级高于方法名
   *
   * <span class="token keyword">@return</span> 线程池实例
   */</span>
  <span class="token annotation punctuation">@DynamicTp</span><span class="token punctuation">(</span><span class="token string">"commonExecutor"</span><span class="token punctuation">)</span>
  <span class="token annotation punctuation">@Bean</span>
  <span class="token keyword">public</span> <span class="token class-name">ThreadPoolExecutor</span> <span class="token function">commonExecutor</span><span class="token punctuation">(</span><span class="token punctuation">)</span> <span class="token punctuation">{</span>
      <span class="token keyword">return</span> <span class="token punctuation">(</span><span class="token class-name">ThreadPoolExecutor</span><span class="token punctuation">)</span> <span class="token class-name">Executors</span><span class="token punctuation">.</span><span class="token function">newFixedThreadPool</span><span class="token punctuation">(</span><span class="token number">1</span><span class="token punctuation">)</span><span class="token punctuation">;</span>
  <span class="token punctuation">}</span>

  <span class="token doc-comment comment">/**
   * 通过<span class="token punctuation">{</span><span class="token keyword">@link</span> <span class="token reference"><span class="token class-name">ThreadPoolCreator</span></span><span class="token punctuation">}</span> 快速创建一些简单配置的动态线程池
   * tips: 建议直接在配置中心配置就行，不用@Bean声明
   *
   * <span class="token keyword">@return</span> 线程池实例
   */</span>
  <span class="token annotation punctuation">@Bean</span>
  <span class="token keyword">public</span> <span class="token class-name">DtpExecutor</span> <span class="token function">dtpExecutor1</span><span class="token punctuation">(</span><span class="token punctuation">)</span> <span class="token punctuation">{</span>
      <span class="token keyword">return</span> <span class="token class-name">ThreadPoolCreator</span><span class="token punctuation">.</span><span class="token function">createDynamicFast</span><span class="token punctuation">(</span><span class="token string">"dtpExecutor1"</span><span class="token punctuation">)</span><span class="token punctuation">;</span>
  <span class="token punctuation">}</span>

  <span class="token doc-comment comment">/**
   * 通过<span class="token punctuation">{</span><span class="token keyword">@link</span> <span class="token reference"><span class="token class-name">ThreadPoolBuilder</span></span><span class="token punctuation">}</span> 设置详细参数创建动态线程池（推荐方式），
   * ioIntensive，参考tomcat线程池设计，实现了处理io密集型任务的线程池，具体参数可以看代码注释
   *
   * tips: 建议直接在配置中心配置就行，不用@Bean声明
   * <span class="token keyword">@return</span> 线程池实例
   */</span>
  <span class="token annotation punctuation">@Bean</span>
  <span class="token keyword">public</span> <span class="token class-name">DtpExecutor</span> <span class="token function">ioIntensiveExecutor</span><span class="token punctuation">(</span><span class="token punctuation">)</span> <span class="token punctuation">{</span>
      <span class="token keyword">return</span> <span class="token class-name">ThreadPoolBuilder</span><span class="token punctuation">.</span><span class="token function">newBuilder</span><span class="token punctuation">(</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">threadPoolName</span><span class="token punctuation">(</span><span class="token string">"ioIntensiveExecutor"</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">corePoolSize</span><span class="token punctuation">(</span><span class="token number">20</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">maximumPoolSize</span><span class="token punctuation">(</span><span class="token number">50</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">queueCapacity</span><span class="token punctuation">(</span><span class="token number">2048</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">ioIntensive</span><span class="token punctuation">(</span><span class="token boolean">true</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">buildDynamic</span><span class="token punctuation">(</span><span class="token punctuation">)</span><span class="token punctuation">;</span>
  <span class="token punctuation">}</span>

  <span class="token doc-comment comment">/**
   * tips: 建议直接在配置中心配置就行，不用@Bean声明
   * <span class="token keyword">@return</span> 线程池实例
   */</span>
  <span class="token annotation punctuation">@Bean</span>
  <span class="token keyword">public</span> <span class="token class-name">ThreadPoolExecutor</span> <span class="token function">dtpExecutor2</span><span class="token punctuation">(</span><span class="token punctuation">)</span> <span class="token punctuation">{</span>
      <span class="token keyword">return</span> <span class="token class-name">ThreadPoolBuilder</span><span class="token punctuation">.</span><span class="token function">newBuilder</span><span class="token punctuation">(</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">threadPoolName</span><span class="token punctuation">(</span><span class="token string">"dtpExecutor2"</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">corePoolSize</span><span class="token punctuation">(</span><span class="token number">10</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">maximumPoolSize</span><span class="token punctuation">(</span><span class="token number">15</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">keepAliveTime</span><span class="token punctuation">(</span><span class="token number">50</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">timeUnit</span><span class="token punctuation">(</span><span class="token class-name">TimeUnit</span><span class="token punctuation">.</span>MILLISECONDS<span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">workQueue</span><span class="token punctuation">(</span><span class="token class-name">QueueTypeEnum</span><span class="token punctuation">.</span>SYNCHRONOUS_QUEUE<span class="token punctuation">.</span><span class="token function">getName</span><span class="token punctuation">(</span><span class="token punctuation">)</span><span class="token punctuation">,</span> <span class="token keyword">null</span><span class="token punctuation">,</span> <span class="token boolean">false</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">waitForTasksToCompleteOnShutdown</span><span class="token punctuation">(</span><span class="token boolean">true</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">awaitTerminationSeconds</span><span class="token punctuation">(</span><span class="token number">5</span><span class="token punctuation">)</span>
              <span class="token punctuation">.</span><span class="token function">buildDynamic</span><span class="token punctuation">(</span><span class="token punctuation">)</span><span class="token punctuation">;</span>
  <span class="token punctuation">}</span>
<span class="token punctuation">}</span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></li>
<li>
<p>代码调用</p>
<p>从DtpRegistry中根据线程池名称获取，或者通过依赖注入方式(推荐，更优雅)</p>
<p>1）依赖注入方式使用，优先推荐依赖注入方式，不能使用依赖注入的场景可以使用方式2</p>
<div class="language-java ext-java line-numbers-mode"><pre v-pre class="language-java"><code><span class="token annotation punctuation">@Resource</span>
<span class="token keyword">private</span> <span class="token class-name">ThreadPoolExecutor</span> dtpExecutor1<span class="token punctuation">;</span>

<span class="token keyword">public</span> <span class="token keyword">void</span> <span class="token function">exec</span><span class="token punctuation">(</span><span class="token punctuation">)</span> <span class="token punctuation">{</span>
   dtpExecutor1<span class="token punctuation">.</span><span class="token function">execute</span><span class="token punctuation">(</span><span class="token punctuation">(</span><span class="token punctuation">)</span> <span class="token operator">-></span> <span class="token class-name">System</span><span class="token punctuation">.</span>out<span class="token punctuation">.</span><span class="token function">println</span><span class="token punctuation">(</span><span class="token string">"test"</span><span class="token punctuation">)</span><span class="token punctuation">)</span><span class="token punctuation">;</span>
<span class="token punctuation">}</span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><p>2）通过DtpRegistry注册器获取</p>
<div class="language-java ext-java line-numbers-mode"><pre v-pre class="language-java"><code><span class="token keyword">public</span> <span class="token keyword">static</span> <span class="token keyword">void</span> <span class="token function">main</span><span class="token punctuation">(</span><span class="token class-name">String</span><span class="token punctuation">[</span><span class="token punctuation">]</span> args<span class="token punctuation">)</span> <span class="token punctuation">{</span>
   <span class="token class-name">DtpExecutor</span> dtpExecutor <span class="token operator">=</span> <span class="token class-name">DtpRegistry</span><span class="token punctuation">.</span><span class="token function">getDtpExecutor</span><span class="token punctuation">(</span><span class="token string">"dtpExecutor1"</span><span class="token punctuation">)</span><span class="token punctuation">;</span>
   dtpExecutor<span class="token punctuation">.</span><span class="token function">execute</span><span class="token punctuation">(</span><span class="token punctuation">(</span><span class="token punctuation">)</span> <span class="token operator">-></span> <span class="token class-name">System</span><span class="token punctuation">.</span>out<span class="token punctuation">.</span><span class="token function">println</span><span class="token punctuation">(</span><span class="token string">"test"</span><span class="token punctuation">)</span><span class="token punctuation">)</span><span class="token punctuation">;</span>
<span class="token punctuation">}</span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></li>
<li>
<p>更详细使用实例请参考<code v-pre>example</code>工程</p>
</li>
</ul>
</div></template>
