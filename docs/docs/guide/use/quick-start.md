---
title: 接入步骤
icon: launch
order: 1
author: yanhom
date: 2022-06-11
category:
  - 接入步骤
tag:
  - 接入步骤
sticky: true
star: true

---

**接入步骤**

1.引入相应配置中心的依赖，具体见下述 mavne依赖

2.配置中心配置线程池实例，配置见下述（给出的是全配置项，不用的可以删除）

3.启动类加 @EnableDynamicTp 注解

4.使用 @Resource 或 @Autowired 进行依赖注入，或通过 DtpRegistry.getDtpExecutor("name")获取

5.通过以上4步就可以使用了，是不是感觉很简单啊


::: tip 启动日志输出

服务启动看到有如下日志输出说明接入成功

  ```bash
  |  __ \                            (_) |__   __|
  | |  | |_   _ _ __   __ _ _ __ ___  _  ___| |_ __
  | |  | | | | | '_ \ / _` | '_ ` _ | |/ __| | '_ \
  | |__| | |_| | | | | (_| | | | | | | | (__| | |_) |
  |_____/ __, |_| |_|__,_|_| |_| |_|_|___|_| .__/
           __/ |                              | |
          |___/                               |_|
   :: Dynamic Thread Pool ::

  DynamicTp register dtpExecutor, source: beanPostProcessor, executor: DtpMainPropWrapper(dtpName=dynamic-tp-test-1, corePoolSize=6, maxPoolSize=8, keepAliveTime=50, queueType=VariableLinkedBlockingQueue, queueCapacity=200, rejectType=RejectedCountableCallerRunsPolicy, allowCoreThreadTimeOut=false)

  DtpRegistry initialization end, remote dtpExecutors: [dtpExecutor1, dtpExecutor2], local dtpExecutors: [ioIntensiveExecutor], local commonExecutors: [commonExecutor]
  ```

:::

::: warning

1.普通 JUC 线程池想要被监控，可以 @Bean 定义时加 @DynamicTp 注解

2.动态线程池实例服务启动时会根据配置中心的配置动态注册到Spring容器中，建议不要用@Bean编程式重复声明同一线程池实例，直接配置在配置中心就行

3.阻塞队列只有 VariableLinkedBlockingQueue 类型可以修改 capacity，该类型功能和 LinkedBlockingQueue 相似， 只是 capacity 不是
final 类型，可以修改， VariableLinkedBlockingQueue 参考 RabbitMq 的实现
:::

