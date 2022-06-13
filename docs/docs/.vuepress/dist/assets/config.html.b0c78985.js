import{_ as n}from"./plugin-vue_export-helper.21dcd24c.js";import{o as e,c as s,d as a}from"./app.4a89ddb7.js";const t={},o=a(`<div class="custom-container tip"><p class="custom-container-title">\u63D0\u793A</p><p>1.\u52A8\u6001\u7EBF\u7A0B\u6C60\u914D\u7F6E\u6587\u4EF6\uFF0C\u5EFA\u8BAE\u5355\u72EC\u5F00\u4E00\u4E2A\u6587\u4EF6\u653E\u5230\u914D\u7F6E\u4E2D\u5FC3</p><p>2.\u5EFA\u8BAE\u6700\u597D\u4F7F\u7528yml\u6587\u4EF6\u914D\u7F6E\uFF0C\u53EF\u8BFB\u6027\u3001\u53EF\u64CD\u4F5C\u6027\u66F4\u53CB\u597D</p><p>3.\u7ED9\u51FA\u7684\u662F\u5168\u914D\u7F6E\u9879\uFF0C\u4F7F\u7528\u4E0D\u5230\u7684\u9879\u6216\u8005\u4F7F\u7528\u9ED8\u8BA4\u503C\u7684\u9879\u90FD\u53EF\u4EE5\u5220\u9664\uFF0C\u51CF\u5C11\u914D\u7F6E\u9879</p></div><div class="custom-container danger"><p class="custom-container-title">\u8B66\u544A</p><p>1.\u4E0B\u8FF0\u914D\u7F6E\u9879\u7684\u503C\u90FD\u662F\u968F\u4FBF\u586B\u5199\u7684\uFF0C\u8BF7\u4E0D\u8981\u76F4\u63A5\u4F7F\u7528\u8BE5\u503C\uFF0C\u6839\u636E\u81EA\u5DF1\u9879\u76EE\u505A\u8C03\u6574</p></div><ul><li>\u7EBF\u7A0B\u6C60\u914D\u7F6E\uFF08yml \u7C7B\u578B\uFF09</li></ul><div class="language-yaml ext-yml line-numbers-mode"><pre class="language-yaml"><code><span class="token key atrule">spring</span><span class="token punctuation">:</span>
  <span class="token key atrule">dynamic</span><span class="token punctuation">:</span>
    <span class="token key atrule">tp</span><span class="token punctuation">:</span>
      <span class="token key atrule">enabled</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>
      <span class="token key atrule">enabledBanner</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>           <span class="token comment"># \u662F\u5426\u5F00\u542Fbanner\u6253\u5370\uFF0C\u9ED8\u8BA4true</span>
      <span class="token key atrule">enabledCollect</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>          <span class="token comment"># \u662F\u5426\u5F00\u542F\u76D1\u63A7\u6307\u6807\u91C7\u96C6\uFF0C\u9ED8\u8BA4false</span>
      <span class="token key atrule">collectorType</span><span class="token punctuation">:</span> micrometer     <span class="token comment"># \u76D1\u63A7\u6570\u636E\u91C7\u96C6\u5668\u7C7B\u578B\uFF08jsonlog | micrometer\uFF09\uFF0C\u9ED8\u8BA4logging</span>
      <span class="token key atrule">logPath</span><span class="token punctuation">:</span> /home/logs           <span class="token comment"># \u76D1\u63A7\u65E5\u5FD7\u6570\u636E\u8DEF\u5F84\uFF0C\u9ED8\u8BA4 \${user.home}/logs\uFF0C\u91C7\u96C6\u7C7B\u578B\u975Ejsonlog\u4E0D\u7528\u914D\u7F6E</span>
      <span class="token key atrule">monitorInterval</span><span class="token punctuation">:</span> <span class="token number">5</span>            <span class="token comment"># \u76D1\u63A7\u65F6\u95F4\u95F4\u9694\uFF08\u62A5\u8B66\u5224\u65AD\u3001\u6307\u6807\u91C7\u96C6\uFF09\uFF0C\u9ED8\u8BA45s</span>
      <span class="token key atrule">nacos</span><span class="token punctuation">:</span>                        <span class="token comment"># nacos\u914D\u7F6E\uFF0C\u4E0D\u914D\u7F6E\u6709\u9ED8\u8BA4\u503C\uFF08\u89C4\u5219appname-dev.yml\u8FD9\u6837\uFF09\uFF0Ccloud\u5E94\u7528\u4E0D\u9700\u8981\u914D\u7F6E</span>
        <span class="token key atrule">dataId</span><span class="token punctuation">:</span> dynamic<span class="token punctuation">-</span>tp<span class="token punctuation">-</span>demo<span class="token punctuation">-</span>dev.yml
        <span class="token key atrule">group</span><span class="token punctuation">:</span> DEFAULT_GROUP
      <span class="token key atrule">apollo</span><span class="token punctuation">:</span>                       <span class="token comment"># apollo\u914D\u7F6E\uFF0C\u4E0D\u914D\u7F6E\u9ED8\u8BA4\u62FFapollo\u914D\u7F6E\u7B2C\u4E00\u4E2Anamespace</span>
        <span class="token key atrule">namespace</span><span class="token punctuation">:</span> dynamic<span class="token punctuation">-</span>tp<span class="token punctuation">-</span>demo<span class="token punctuation">-</span>dev.yml
      <span class="token key atrule">configType</span><span class="token punctuation">:</span> yml               <span class="token comment"># \u914D\u7F6E\u6587\u4EF6\u7C7B\u578B\uFF0C\u975Ecloud nacos \u548C apollo\u9700\u914D\u7F6E\uFF0C\u5176\u4ED6\u4E0D\u7528\u914D</span>
      <span class="token key atrule">platforms</span><span class="token punctuation">:</span>                    <span class="token comment"># \u901A\u77E5\u62A5\u8B66\u5E73\u53F0\u914D\u7F6E</span>
        <span class="token punctuation">-</span> <span class="token key atrule">platform</span><span class="token punctuation">:</span> wechat
          <span class="token key atrule">urlKey</span><span class="token punctuation">:</span> 3a700<span class="token punctuation">-</span>127<span class="token punctuation">-</span>4bd<span class="token punctuation">-</span>a798<span class="token punctuation">-</span>c53d8b69c     <span class="token comment"># \u66FF\u6362</span>
          <span class="token key atrule">receivers</span><span class="token punctuation">:</span> test1<span class="token punctuation">,</span>test2                   <span class="token comment"># \u63A5\u53D7\u4EBA\u4F01\u5FAE\u540D\u79F0</span>
        <span class="token punctuation">-</span> <span class="token key atrule">platform</span><span class="token punctuation">:</span> ding
          <span class="token key atrule">urlKey</span><span class="token punctuation">:</span> f80dad441fcd655438f4a08dcd6a     <span class="token comment"># \u66FF\u6362</span>
          <span class="token key atrule">secret</span><span class="token punctuation">:</span> SECb5441fa6f375d5b9d21           <span class="token comment"># \u66FF\u6362\uFF0C\u975Esign\u6A21\u5F0F\u53EF\u4EE5\u6CA1\u6709\u6B64\u503C</span>
          <span class="token key atrule">receivers</span><span class="token punctuation">:</span> <span class="token number">18888888888</span>                   <span class="token comment"># \u9489\u9489\u8D26\u53F7\u624B\u673A\u53F7</span>
        <span class="token punctuation">-</span> <span class="token key atrule">platform</span><span class="token punctuation">:</span> lark
          <span class="token key atrule">urlKey</span><span class="token punctuation">:</span> 0d944ae7<span class="token punctuation">-</span>b24a<span class="token punctuation">-</span><span class="token number">40</span>                 <span class="token comment"># \u66FF\u6362</span>
          <span class="token key atrule">receivers</span><span class="token punctuation">:</span> test1<span class="token punctuation">,</span>test2                   <span class="token comment"># \u63A5\u53D7\u4EBA\u98DE\u4E66\u540D\u79F0/openid</span>
      <span class="token key atrule">tomcatTp</span><span class="token punctuation">:</span>                                    <span class="token comment"># tomcat webserver\u7EBF\u7A0B\u6C60\u914D\u7F6E</span>
        <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">100</span>
        <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
        <span class="token key atrule">keepAliveTime</span><span class="token punctuation">:</span> <span class="token number">60</span>
      <span class="token key atrule">jettyTp</span><span class="token punctuation">:</span>                                     <span class="token comment"># jetty weberver\u7EBF\u7A0B\u6C60\u914D\u7F6E</span>
        <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">100</span>
        <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
      <span class="token key atrule">undertowTp</span><span class="token punctuation">:</span>                                  <span class="token comment"># undertow webserver\u7EBF\u7A0B\u6C60\u914D\u7F6E</span>
        <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">100</span>
        <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
        <span class="token key atrule">keepAliveTime</span><span class="token punctuation">:</span> <span class="token number">60</span>
      <span class="token key atrule">hystrixTp</span><span class="token punctuation">:</span>                                   <span class="token comment"># hystrix \u7EBF\u7A0B\u6C60\u914D\u7F6E</span>
        <span class="token punctuation">-</span> <span class="token key atrule">threadPoolName</span><span class="token punctuation">:</span> hystrix1
          <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">100</span>
          <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
          <span class="token key atrule">keepAliveTime</span><span class="token punctuation">:</span> <span class="token number">60</span>
      <span class="token key atrule">dubboTp</span><span class="token punctuation">:</span>                                     <span class="token comment"># dubbo \u7EBF\u7A0B\u6C60\u914D\u7F6E</span>
        <span class="token punctuation">-</span> <span class="token key atrule">threadPoolName</span><span class="token punctuation">:</span> dubboTp<span class="token comment">#20880            # \u540D\u79F0\u89C4\u5219\uFF1AdubboTp + &quot;#&quot; + \u534F\u8BAE\u7AEF\u53E3</span>
          <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">100</span>
          <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
          <span class="token key atrule">keepAliveTime</span><span class="token punctuation">:</span> <span class="token number">60</span>
      <span class="token key atrule">rocketMqTp</span><span class="token punctuation">:</span>                                  <span class="token comment"># rocketmq \u7EBF\u7A0B\u6C60\u914D\u7F6E</span>
        <span class="token punctuation">-</span> <span class="token key atrule">threadPoolName</span><span class="token punctuation">:</span> group1<span class="token comment">#topic1            # \u540D\u79F0\u89C4\u5219\uFF1Agroup + &quot;#&quot; + topic</span>
          <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
          <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">200</span>
          <span class="token key atrule">keepAliveTime</span><span class="token punctuation">:</span> <span class="token number">60</span>
      <span class="token key atrule">executors</span><span class="token punctuation">:</span>                                   <span class="token comment"># \u52A8\u6001\u7EBF\u7A0B\u6C60\u914D\u7F6E\uFF0C\u90FD\u6709\u9ED8\u8BA4\u503C\uFF0C\u91C7\u7528\u9ED8\u8BA4\u503C\u7684\u53EF\u4EE5\u4E0D\u914D\u7F6E\u8BE5\u9879\uFF0C\u51CF\u5C11\u914D\u7F6E\u91CF</span>
        <span class="token punctuation">-</span> <span class="token key atrule">threadPoolName</span><span class="token punctuation">:</span> dtpExecutor1
          <span class="token key atrule">executorType</span><span class="token punctuation">:</span> common                     <span class="token comment"># \u7EBF\u7A0B\u6C60\u7C7B\u578Bcommon\u3001eager\uFF1A\u9002\u7528\u4E8Eio\u5BC6\u96C6\u578B</span>
          <span class="token key atrule">corePoolSize</span><span class="token punctuation">:</span> <span class="token number">6</span>
          <span class="token key atrule">maximumPoolSize</span><span class="token punctuation">:</span> <span class="token number">8</span>
          <span class="token key atrule">queueCapacity</span><span class="token punctuation">:</span> <span class="token number">200</span>
          <span class="token key atrule">queueType</span><span class="token punctuation">:</span> VariableLinkedBlockingQueue   <span class="token comment"># \u4EFB\u52A1\u961F\u5217\uFF0C\u67E5\u770B\u6E90\u7801QueueTypeEnum\u679A\u4E3E\u7C7B</span>
          <span class="token key atrule">rejectedHandlerType</span><span class="token punctuation">:</span> CallerRunsPolicy    <span class="token comment"># \u62D2\u7EDD\u7B56\u7565\uFF0C\u67E5\u770BRejectedTypeEnum\u679A\u4E3E\u7C7B</span>
          <span class="token key atrule">keepAliveTime</span><span class="token punctuation">:</span> <span class="token number">50</span>
          <span class="token key atrule">allowCoreThreadTimeOut</span><span class="token punctuation">:</span> <span class="token boolean important">false</span>                  <span class="token comment"># \u662F\u5426\u5141\u8BB8\u6838\u5FC3\u7EBF\u7A0B\u6C60\u8D85\u65F6</span>
          <span class="token key atrule">threadNamePrefix</span><span class="token punctuation">:</span> test                         <span class="token comment"># \u7EBF\u7A0B\u540D\u524D\u7F00</span>
          <span class="token key atrule">waitForTasksToCompleteOnShutdown</span><span class="token punctuation">:</span> <span class="token boolean important">false</span>        <span class="token comment"># \u53C2\u8003spring\u7EBF\u7A0B\u6C60\u8BBE\u8BA1\uFF0C\u4F18\u96C5\u5173\u95ED\u7EBF\u7A0B\u6C60</span>
          <span class="token key atrule">awaitTerminationSeconds</span><span class="token punctuation">:</span> <span class="token number">5</span>                     <span class="token comment"># \u5355\u4F4D\uFF08s\uFF09</span>
          <span class="token key atrule">preStartAllCoreThreads</span><span class="token punctuation">:</span> <span class="token boolean important">false</span>                  <span class="token comment"># \u662F\u5426\u9884\u70ED\u6240\u6709\u6838\u5FC3\u7EBF\u7A0B\uFF0C\u9ED8\u8BA4false</span>
          <span class="token key atrule">runTimeout</span><span class="token punctuation">:</span> <span class="token number">200</span>                                <span class="token comment"># \u4EFB\u52A1\u6267\u884C\u8D85\u65F6\u9608\u503C\uFF0C\u76EE\u524D\u53EA\u505A\u544A\u8B66\u7528\uFF0C\u5355\u4F4D\uFF08ms\uFF09</span>
          <span class="token key atrule">queueTimeout</span><span class="token punctuation">:</span> <span class="token number">100</span>                              <span class="token comment"># \u4EFB\u52A1\u5728\u961F\u5217\u7B49\u5F85\u8D85\u65F6\u9608\u503C\uFF0C\u76EE\u524D\u53EA\u505A\u544A\u8B66\u7528\uFF0C\u5355\u4F4D\uFF08ms\uFF09</span>
          <span class="token key atrule">taskWrapperNames</span><span class="token punctuation">:</span> <span class="token punctuation">[</span><span class="token string">&quot;ttl&quot;</span><span class="token punctuation">]</span>                          <span class="token comment"># \u4EFB\u52A1\u5305\u88C5\u5668\u540D\u79F0\uFF0C\u96C6\u6210TaskWrapper\u63A5\u53E3</span>
          <span class="token key atrule">notifyItems</span><span class="token punctuation">:</span>                     <span class="token comment"># \u62A5\u8B66\u9879\uFF0C\u4E0D\u914D\u7F6E\u81EA\u52A8\u4F1A\u6309\u9ED8\u8BA4\u503C\u914D\u7F6E\uFF08\u53D8\u66F4\u901A\u77E5\u3001\u5BB9\u91CF\u62A5\u8B66\u3001\u6D3B\u6027\u62A5\u8B66\u3001\u62D2\u7EDD\u62A5\u8B66\u3001\u4EFB\u52A1\u8D85\u65F6\u62A5\u8B66\uFF09</span>
            <span class="token punctuation">-</span> <span class="token key atrule">type</span><span class="token punctuation">:</span> capacity               <span class="token comment"># \u62A5\u8B66\u9879\u7C7B\u578B\uFF0C\u67E5\u770B\u6E90\u7801 NotifyTypeEnum\u679A\u4E3E\u7C7B</span>
              <span class="token key atrule">enabled</span><span class="token punctuation">:</span> <span class="token boolean important">true</span>
              <span class="token key atrule">threshold</span><span class="token punctuation">:</span> <span class="token number">80</span>                <span class="token comment"># \u62A5\u8B66\u9608\u503C</span>
              <span class="token key atrule">platforms</span><span class="token punctuation">:</span> <span class="token punctuation">[</span>ding<span class="token punctuation">,</span>wechat<span class="token punctuation">]</span>     <span class="token comment"># \u53EF\u9009\u914D\u7F6E\uFF0C\u4E0D\u914D\u7F6E\u9ED8\u8BA4\u62FF\u4E0A\u5C42platforms\u914D\u7F6E\u7684\u6240\u4EE5\u5E73\u53F0</span>
              <span class="token key atrule">interval</span><span class="token punctuation">:</span> <span class="token number">120</span>                <span class="token comment"># \u62A5\u8B66\u95F4\u9694\uFF08\u5355\u4F4D\uFF1As\uFF09</span>
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
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><ul><li><p>\u7EBF\u7A0B\u6C60\u914D\u7F6E\uFF08properties \u7C7B\u578B\uFF09</p><div class="language-properties ext-properties line-numbers-mode"><pre class="language-properties"><code><span class="token key attr-name">    spring.dynamic.tp.enabled</span><span class="token punctuation">=</span><span class="token value attr-value">true</span>
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
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div></li><li><p>\u7EBF\u7A0B\u6C60\u914D\u7F6E\uFF08json \u7C7B\u578B\uFF09</p></li></ul><div class="language-text ext-text line-numbers-mode"><pre class="language-text"><code>{
  &quot;enabled&quot;:true,
  &quot;collectorType&quot;:&quot;logging&quot;,
  &quot;monitorInterval&quot;:5,
  &quot;enabledBanner&quot;:true,
  &quot;enabledCollect&quot;:true,
  &quot;configType&quot;:&quot;json&quot;,
  &quot;zookeeper&quot;:{
    &quot;zkConnectStr&quot;:&quot;127.0.0.1:2181&quot;,
    &quot;rootNode&quot;:&quot;/configserver/dev&quot;,
    &quot;node&quot;:&quot;dynamic-tp-zookeeper-demo&quot;,
    &quot;config-key&quot;:&quot;dtp-config&quot;
  },
  &quot;platforms&quot;:[
    {
      &quot;platform&quot;:&quot;ding&quot;,
      &quot;urlKey&quot;:&quot;aab197577f6d8dcea6f\\t&quot;,
      &quot;receivers&quot;:&quot;\u6240\u6709\u4EBA&quot;
    }
  ],
  &quot;executors&quot;:[
    {
      &quot;threadPoolName&quot;:&quot;dtpExecutor1&quot;,
      &quot;executorType&quot;:&quot;common&quot;,
      &quot;keepAliveTime&quot;:20,
      &quot;waitForTasksToCompleteOnShutdown&quot;:false,
      &quot;rejectedHandlerType&quot;:&quot;AbortPolicy&quot;,
      &quot;queueCapacity&quot;:1000,
      &quot;fair&quot;:false,
      &quot;unit&quot;:&quot;SECONDS&quot;,
      &quot;runTimeout&quot;:300,
      &quot;threadNamePrefix&quot;:&quot;t0&quot;,
      &quot;allowCoreThreadTimeOut&quot;:false,
      &quot;corePoolSize&quot;:15,
      &quot;queueType&quot;:&quot;VariableLinkedBlockingQueue&quot;,
      &quot;maximumPoolSize&quot;:30,
      &quot;awaitTerminationSeconds&quot;:1,
      &quot;preStartAllCoreThreads&quot;:true,
      &quot;notifyItems&quot;:[],
      &quot;queueTimeout&quot;:300
    },
    {
      &quot;threadPoolName&quot;:&quot;dtpExecutor2&quot;,
      &quot;executorType&quot;:&quot;common&quot;,
      &quot;keepAliveTime&quot;:20,
      &quot;waitForTasksToCompleteOnShutdown&quot;:false,
      &quot;rejectedHandlerType&quot;:&quot;AbortPolicy&quot;,
      &quot;queueCapacity&quot;:1000,
      &quot;fair&quot;:false,
      &quot;unit&quot;:&quot;SECONDS&quot;,
      &quot;runTimeout&quot;:300,
      &quot;threadNamePrefix&quot;:&quot;t1&quot;,
      &quot;allowCoreThreadTimeOut&quot;:false,
      &quot;corePoolSize&quot;:20,
      &quot;queueType&quot;:&quot;VariableLinkedBlockingQueue&quot;,
      &quot;maximumPoolSize&quot;:20,
      &quot;awaitTerminationSeconds&quot;:1,
      &quot;preStartAllCoreThreads&quot;:true,
      &quot;notifyItems&quot;:[],
      &quot;queueTimeout&quot;:300
    }
  ]
}
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><ul><li>\u7EBF\u7A0B\u6C60\u914D\u7F6E\uFF08\u7528\u4E8Ezk\u5DE5\u5177\u4E00\u952E\u5BFC\u5165\uFF09</li></ul><div class="language-text ext-text line-numbers-mode"><pre class="language-text"><code>/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.enabled=true
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
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div>`,8),i=[o];function p(l,c){return e(),s("div",null,i)}var d=n(t,[["render",p],["__file","config.html.vue"]]);export{d as default};
