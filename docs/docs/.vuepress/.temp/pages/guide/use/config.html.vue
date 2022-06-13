<template><div><div class="custom-container tip"><p class="custom-container-title">提示</p>
<p>1.动态线程池配置文件，建议单独开一个文件放到配置中心</p>
<p>2.建议最好使用yml文件配置，可读性、可操作性更友好</p>
<p>3.给出的是全配置项，使用不到的项或者使用默认值的项都可以删除，减少配置项</p>
</div>
<div class="custom-container danger"><p class="custom-container-title">警告</p>
<p>1.下述配置项的值都是随便填写的，请不要直接使用该值，根据自己项目做调整</p>
</div>
<ul>
<li>线程池配置（yml 类型）</li>
</ul>
<div class="language-yaml ext-yml line-numbers-mode"><pre v-pre class="language-yaml"><code><span class="token key atrule">spring</span><span class="token punctuation">:</span>
  <span class="token key atrule">dynamic</span><span class="token punctuation">:</span>
    <span class="token key atrule">tp</span><span class="token punctuation">:</span>
      <span class="token key atrule">enabled</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>
      <span class="token key atrule">enabledBanner</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>           <span class="token comment"># 是否开启banner打印，默认true</span>
      <span class="token key atrule">enabledCollect</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>          <span class="token comment"># 是否开启监控指标采集，默认false</span>
      <span class="token key atrule">collectorType</span><span class="token punctuation">:</span> micrometer     <span class="token comment"># 监控数据采集器类型（jsonlog | micrometer），默认logging</span>
      <span class="token key atrule">logPath</span><span class="token punctuation">:</span> /home/logs           <span class="token comment"># 监控日志数据路径，默认 ${user.home}/logs，采集类型非jsonlog不用配置</span>
      <span class="token key atrule">monitorInterval</span><span class="token punctuation">:</span> <span class="token number">5</span>            <span class="token comment"># 监控时间间隔（报警判断、指标采集），默认5s</span>
      <span class="token key atrule">nacos</span><span class="token punctuation">:</span>                        <span class="token comment"># nacos配置，不配置有默认值（规则appname-dev.yml这样），cloud应用不需要配置</span>
        <span class="token key atrule">dataId</span><span class="token punctuation">:</span> dynamic<span class="token punctuation">-</span>tp<span class="token punctuation">-</span>demo<span class="token punctuation">-</span>dev.yml
        <span class="token key atrule">group</span><span class="token punctuation">:</span> DEFAULT_GROUP
      <span class="token key atrule">apollo</span><span class="token punctuation">:</span>                       <span class="token comment"># apollo配置，不配置默认拿apollo配置第一个namespace</span>
        <span class="token key atrule">namespace</span><span class="token punctuation">:</span> dynamic<span class="token punctuation">-</span>tp<span class="token punctuation">-</span>demo<span class="token punctuation">-</span>dev.yml
      <span class="token key atrule">configType</span><span class="token punctuation">:</span> yml               <span class="token comment"># 配置文件类型，非cloud nacos 和 apollo需配置，其他不用配</span>
      <span class="token key atrule">platforms</span><span class="token punctuation">:</span>                    <span class="token comment"># 通知报警平台配置</span>
        <span class="token punctuation">-</span> <span class="token key atrule">platform</span><span class="token punctuation">:</span> wechat
          <span class="token key atrule">urlKey</span><span class="token punctuation">:</span> 3a700<span class="token punctuation">-</span>127<span class="token punctuation">-</span>4bd<span class="token punctuation">-</span>a798<span class="token punctuation">-</span>c53d8b69c     <span class="token comment"># 替换</span>
          <span class="token key atrule">receivers</span><span class="token punctuation">:</span> test1<span class="token punctuation">,</span>test2                   <span class="token comment"># 接受人企微名称</span>
        <span class="token punctuation">-</span> <span class="token key atrule">platform</span><span class="token punctuation">:</span> ding
          <span class="token key atrule">urlKey</span><span class="token punctuation">:</span> f80dad441fcd655438f4a08dcd6a     <span class="token comment"># 替换</span>
          <span class="token key atrule">secret</span><span class="token punctuation">:</span> SECb5441fa6f375d5b9d21           <span class="token comment"># 替换，非sign模式可以没有此值</span>
          <span class="token key atrule">receivers</span><span class="token punctuation">:</span> <span class="token number">18888888888</span>                   <span class="token comment"># 钉钉账号手机号</span>
        <span class="token punctuation">-</span> <span class="token key atrule">platform</span><span class="token punctuation">:</span> lark
          <span class="token key atrule">urlKey</span><span class="token punctuation">:</span> 0d944ae7<span class="token punctuation">-</span>b24a<span class="token punctuation">-</span><span class="token number">40</span>                 <span class="token comment"># 替换</span>
          <span class="token key atrule">receivers</span><span class="token punctuation">:</span> test1<span class="token punctuation">,</span>test2                   <span class="token comment"># 接受人飞书名称/openid</span>
      <span class="token key atrule">tomcatTp</span><span class="token punctuation">:</span>                                    <span class="token comment"># tomcat webserver线程池配置</span>
        <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">100</span>
        <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
        <span class="token key atrule">keepAliveTime</span><span class="token punctuation">:</span> <span class="token number">60</span>
      <span class="token key atrule">jettyTp</span><span class="token punctuation">:</span>                                     <span class="token comment"># jetty weberver线程池配置</span>
        <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">100</span>
        <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
      <span class="token key atrule">undertowTp</span><span class="token punctuation">:</span>                                  <span class="token comment"># undertow webserver线程池配置</span>
        <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">100</span>
        <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
        <span class="token key atrule">keepAliveTime</span><span class="token punctuation">:</span> <span class="token number">60</span>
      <span class="token key atrule">hystrixTp</span><span class="token punctuation">:</span>                                   <span class="token comment"># hystrix 线程池配置</span>
        <span class="token punctuation">-</span> <span class="token key atrule">threadPoolName</span><span class="token punctuation">:</span> hystrix1
          <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">100</span>
          <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
          <span class="token key atrule">keepAliveTime</span><span class="token punctuation">:</span> <span class="token number">60</span>
      <span class="token key atrule">dubboTp</span><span class="token punctuation">:</span>                                     <span class="token comment"># dubbo 线程池配置</span>
        <span class="token punctuation">-</span> <span class="token key atrule">threadPoolName</span><span class="token punctuation">:</span> dubboTp<span class="token comment">#20880            # 名称规则：dubboTp + "#" + 协议端口</span>
          <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">100</span>
          <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
          <span class="token key atrule">keepAliveTime</span><span class="token punctuation">:</span> <span class="token number">60</span>
      <span class="token key atrule">rocketMqTp</span><span class="token punctuation">:</span>                                  <span class="token comment"># rocketmq 线程池配置</span>
        <span class="token punctuation">-</span> <span class="token key atrule">threadPoolName</span><span class="token punctuation">:</span> group1<span class="token comment">#topic1            # 名称规则：group + "#" + topic</span>
          <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
          <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
          <span class="token key atrule">keepAliveTime</span><span class="token punctuation">:</span> <span class="token number">60</span>
      <span class="token key atrule">executors</span><span class="token punctuation">:</span>                                   <span class="token comment"># 动态线程池配置，都有默认值，采用默认值的可以不配置该项，减少配置量</span>
        <span class="token punctuation">-</span> <span class="token key atrule">threadPoolName</span><span class="token punctuation">:</span> dtpExecutor1
          <span class="token key atrule">executorType</span><span class="token punctuation">:</span> common                     <span class="token comment"># 线程池类型common、eager：适用于io密集型</span>
          <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">6</span>
          <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">8</span>
          <span class="token key atrule">queueCapacity</span><span class="token punctuation">:</span> <span class="token number">200</span>
          <span class="token key atrule">queueType</span><span class="token punctuation">:</span> VariableLinkedBlockingQueue   <span class="token comment"># 任务队列，查看源码QueueTypeEnum枚举类</span>
          <span class="token key atrule">rejectedHandlerType</span><span class="token punctuation">:</span> CallerRunsPolicy    <span class="token comment"># 拒绝策略，查看RejectedTypeEnum枚举类</span>
          <span class="token key atrule">keepAliveTime</span><span class="token punctuation">:</span> <span class="token number">50</span>
          <span class="token key atrule">allowCoreThreadTimeOut</span><span class="token punctuation">:</span> <span class="token boolean important">false</span>                  <span class="token comment"># 是否允许核心线程池超时</span>
          <span class="token key atrule">threadNamePrefix</span><span class="token punctuation">:</span> test                         <span class="token comment"># 线程名前缀</span>
          <span class="token key atrule">waitForTasksToCompleteOnShutdown</span><span class="token punctuation">:</span> <span class="token boolean important">false</span>        <span class="token comment"># 参考spring线程池设计，优雅关闭线程池</span>
          <span class="token key atrule">awaitTerminationSeconds</span><span class="token punctuation">:</span> <span class="token number">5</span>                     <span class="token comment"># 单位（s）</span>
          <span class="token key atrule">preStartAllCoreThreads</span><span class="token punctuation">:</span> <span class="token boolean important">false</span>                  <span class="token comment"># 是否预热所有核心线程，默认false</span>
          <span class="token key atrule">runTimeout</span><span class="token punctuation">:</span> <span class="token number">200</span>                                <span class="token comment"># 任务执行超时阈值，目前只做告警用，单位（ms）</span>
          <span class="token key atrule">queueTimeout</span><span class="token punctuation">:</span> <span class="token number">100</span>                              <span class="token comment"># 任务在队列等待超时阈值，目前只做告警用，单位（ms）</span>
          <span class="token key atrule">taskWrapperNames</span><span class="token punctuation">:</span> <span class="token punctuation">[</span><span class="token string">"ttl"</span><span class="token punctuation">]</span>                          <span class="token comment"># 任务包装器名称，集成TaskWrapper接口</span>
          <span class="token key atrule">notifyItems</span><span class="token punctuation">:</span>                     <span class="token comment"># 报警项，不配置自动会按默认值配置（变更通知、容量报警、活性报警、拒绝报警、任务超时报警）</span>
            <span class="token punctuation">-</span> <span class="token key atrule">type</span><span class="token punctuation">:</span> capacity               <span class="token comment"># 报警项类型，查看源码 NotifyTypeEnum枚举类</span>
              <span class="token key atrule">enabled</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>
              <span class="token key atrule">threshold</span><span class="token punctuation">:</span> <span class="token number">80</span>                <span class="token comment"># 报警阈值</span>
              <span class="token key atrule">platforms</span><span class="token punctuation">:</span> <span class="token punctuation">[</span>ding<span class="token punctuation">,</span>wechat<span class="token punctuation">]</span>     <span class="token comment"># 可选配置，不配置默认拿上层platforms配置的所以平台</span>
              <span class="token key atrule">interval</span><span class="token punctuation">:</span> <span class="token number">120</span>                <span class="token comment"># 报警间隔（单位：s）</span>
            <span class="token punctuation">-</span> <span class="token key atrule">type</span><span class="token punctuation">:</span> change
              <span class="token key atrule">enabled</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>
            <span class="token punctuation">-</span> <span class="token key atrule">type</span><span class="token punctuation">:</span> liveness
              <span class="token key atrule">enabled</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>
              <span class="token key atrule">threshold</span><span class="token punctuation">:</span> <span class="token number">80</span>
            <span class="token punctuation">-</span> <span class="token key atrule">type</span><span class="token punctuation">:</span> reject
              <span class="token key atrule">enabled</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>
              <span class="token key atrule">threshold</span><span class="token punctuation">:</span> <span class="token number">1</span>
            <span class="token punctuation">-</span> <span class="token key atrule">type</span><span class="token punctuation">:</span> run_timeout
              <span class="token key atrule">enabled</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>
              <span class="token key atrule">threshold</span><span class="token punctuation">:</span> <span class="token number">1</span>
            <span class="token punctuation">-</span> <span class="token key atrule">type</span><span class="token punctuation">:</span> queue_timeout
              <span class="token key atrule">enabled</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>
              <span class="token key atrule">threshold</span><span class="token punctuation">:</span> <span class="token number">1</span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><ul>
<li>
<p>线程池配置（properties 类型）</p>
<div class="language-properties ext-properties line-numbers-mode"><pre v-pre class="language-properties"><code><span class="token key attr-name">    spring.dynamic.tp.enabled</span><span class="token punctuation">=</span><span class="token value attr-value">true</span>
<span class="token key attr-name">    spring.dynamic.tp.enabledBanner</span><span class="token punctuation">=</span><span class="token value attr-value">true</span>
<span class="token key attr-name">    spring.dynamic.tp.enabledCollect</span><span class="token punctuation">=</span><span class="token value attr-value">true</span>
<span class="token key attr-name">    spring.dynamic.tp.collectorType</span><span class="token punctuation">=</span><span class="token value attr-value">logging</span>
<span class="token key attr-name">    spring.dynamic.tp.monitorInterval</span><span class="token punctuation">=</span><span class="token value attr-value">5</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].threadPoolName</span><span class="token punctuation">=</span><span class="token value attr-value">dynamic-tp-test-1</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].corePoolSize</span><span class="token punctuation">=</span><span class="token value attr-value">50</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].maximumPoolSize</span><span class="token punctuation">=</span><span class="token value attr-value">50</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].queueCapacity</span><span class="token punctuation">=</span><span class="token value attr-value">3000</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].queueType</span><span class="token punctuation">=</span><span class="token value attr-value">VariableLinkedBlockingQueue</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].rejectedHandlerType</span><span class="token punctuation">=</span><span class="token value attr-value">CallerRunsPolicy</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].keepAliveTime</span><span class="token punctuation">=</span><span class="token value attr-value">50</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].allowCoreThreadTimeOut</span><span class="token punctuation">=</span><span class="token value attr-value">false</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].threadNamePrefix</span><span class="token punctuation">=</span><span class="token value attr-value">test1</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[0].type</span><span class="token punctuation">=</span><span class="token value attr-value">capacity</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[0].enabled</span><span class="token punctuation">=</span><span class="token value attr-value">false</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[0].threshold</span><span class="token punctuation">=</span><span class="token value attr-value">80</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[0].platforms[0]</span><span class="token punctuation">=</span><span class="token value attr-value">ding</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[0].platforms[1]</span><span class="token punctuation">=</span><span class="token value attr-value">wechat</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[0].interval</span><span class="token punctuation">=</span><span class="token value attr-value">120</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[1].type</span><span class="token punctuation">=</span><span class="token value attr-value">change</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[1].enabled</span><span class="token punctuation">=</span><span class="token value attr-value">false</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[2].type</span><span class="token punctuation">=</span><span class="token value attr-value">liveness</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[2].enabled</span><span class="token punctuation">=</span><span class="token value attr-value">false</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[2].threshold</span><span class="token punctuation">=</span><span class="token value attr-value">80</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[3].type</span><span class="token punctuation">=</span><span class="token value attr-value">reject</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[3].enabled</span><span class="token punctuation">=</span><span class="token value attr-value">false</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[0].notifyItems[3].threshold</span><span class="token punctuation">=</span><span class="token value attr-value">1</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[1].threadPoolName</span><span class="token punctuation">=</span><span class="token value attr-value">dynamic-tp-test-2</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[1].corePoolSize</span><span class="token punctuation">=</span><span class="token value attr-value">20</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[1].maximumPoolSize</span><span class="token punctuation">=</span><span class="token value attr-value">30</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[1].queueCapacity</span><span class="token punctuation">=</span><span class="token value attr-value">1000</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[1].queueType</span><span class="token punctuation">=</span><span class="token value attr-value">VariableLinkedBlockingQueue</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[1].rejectedHandlerType</span><span class="token punctuation">=</span><span class="token value attr-value">CallerRunsPolicy</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[1].keepAliveTime</span><span class="token punctuation">=</span><span class="token value attr-value">50</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[1].allowCoreThreadTimeOut</span><span class="token punctuation">=</span><span class="token value attr-value">false</span>
<span class="token key attr-name">    spring.dynamic.tp.executors[1].threadNamePrefix</span><span class="token punctuation">=</span><span class="token value attr-value">test2</span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></li>
<li>
<p>线程池配置（json 类型）</p>
</li>
</ul>
<div class="language-text ext-text line-numbers-mode"><pre v-pre class="language-text"><code>{
  "enabled":true,
  "collectorType":"logging",
  "monitorInterval":5,
  "enabledBanner":true,
  "enabledCollect":true,
  "configType":"json",
  "zookeeper":{
    "zkConnectStr":"127.0.0.1:2181",
    "rootNode":"/configserver/dev",
    "node":"dynamic-tp-zookeeper-demo",
    "config-key":"dtp-config"
  },
  "platforms":[
    {
      "platform":"ding",
      "urlKey":"aab197577f6d8dcea6f\t",
      "receivers":"所有人"
    }
  ],
  "executors":[
    {
      "threadPoolName":"dtpExecutor1",
      "executorType":"common",
      "keepAliveTime":20,
      "waitForTasksToCompleteOnShutdown":false,
      "rejectedHandlerType":"AbortPolicy",
      "queueCapacity":1000,
      "fair":false,
      "unit":"SECONDS",
      "runTimeout":300,
      "threadNamePrefix":"t0",
      "allowCoreThreadTimeOut":false,
      "corePoolSize":15,
      "queueType":"VariableLinkedBlockingQueue",
      "maximumPoolSize":30,
      "awaitTerminationSeconds":1,
      "preStartAllCoreThreads":true,
      "notifyItems":[],
      "queueTimeout":300
    },
    {
      "threadPoolName":"dtpExecutor2",
      "executorType":"common",
      "keepAliveTime":20,
      "waitForTasksToCompleteOnShutdown":false,
      "rejectedHandlerType":"AbortPolicy",
      "queueCapacity":1000,
      "fair":false,
      "unit":"SECONDS",
      "runTimeout":300,
      "threadNamePrefix":"t1",
      "allowCoreThreadTimeOut":false,
      "corePoolSize":20,
      "queueType":"VariableLinkedBlockingQueue",
      "maximumPoolSize":20,
      "awaitTerminationSeconds":1,
      "preStartAllCoreThreads":true,
      "notifyItems":[],
      "queueTimeout":300
    }
  ]
}
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><ul>
<li>线程池配置（用于zk工具一键导入）</li>
</ul>
<div class="language-text ext-text line-numbers-mode"><pre v-pre class="language-text"><code>/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.enabled=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.enabledBanner=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.enabledCollect=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.collectorType=logging
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.monitorInterval=5
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].threadPoolName=dtpExecutor1
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].corePoolSize=50
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].maximumPoolSize=50
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].queueCapacity=3000
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].queueType=VariableLinkedBlockingQueue
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].rejectedHandlerType=CallerRunsPolicy
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].keepAliveTime=50
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].allowCoreThreadTimeOut=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].threadNamePrefix=test1
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[0].type=capacity
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[0].enabled=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[0].threshold=80
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[0].platforms[0]=ding
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[0].platforms[1]=wechat
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[0].interval=120
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[1].type=change
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[1].enabled=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[2].type=liveness
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[2].enabled=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[2].threshold=80
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[3].type=reject
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[3].enabled=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[0].notifyItems[3].threshold=1
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].threadPoolName=dtpExecutor2
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].corePoolSize=20
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].maximumPoolSize=30
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].queueCapacity=1000
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].queueType=VariableLinkedBlockingQueue
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].rejectedHandlerType=CallerRunsPolicy
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].keepAliveTime=50
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].allowCoreThreadTimeOut=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].threadNamePrefix=test2
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[0].type=capacity
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[0].enabled=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[0].threshold=80
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[0].platforms[0]=ding
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[0].platforms[1]=wechat
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[0].interval=120
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[1].type=change
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[1].enabled=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[2].type=liveness
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[2].enabled=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[2].threshold=80
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[3].type=reject
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[3].enabled=true
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.executors[1].notifyItems[3].threshold=1
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.platforms[0].platform=wechat
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.platforms[0].urlKey=38a7e53d8b649c
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.platforms[0].receivers=test
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.platforms[1].platform=ding
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.platforms[1].urlKey=f80dad44d4a8801d593604f4a08dcd6a
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.platforms[1].secret=SECb5444f2c8346741fa6f375d5b9d21
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.platforms[1].receivers=18888888888
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.dubboTp[0].threadPoolName=dubboTp#20880
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.dubboTp[0].corePoolSize=100
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.dubboTp[0].maximumPoolSize=400
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.dubboTp[0].keepAliveTime=40
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.rocketMqTp[0].threadPoolName=test#test
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.rocketMqTp[0].corePoolSize=100
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.rocketMqTp[0].maximumPoolSize=400
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.rocketMqTp[0].keepAliveTime=40
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></div></template>
