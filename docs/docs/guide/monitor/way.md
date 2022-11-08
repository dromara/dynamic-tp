---
title: 采集类型
icon: ability
order: 1
author: yanhom
date: 2022-06-11
category:
  - 采集类型
tag:
  - 采集类型
sticky: true
star: true
---

::: tip 

目前框架提供了四种监控数据采集方式，通过 collectorTypes 属性配置监控指标采集类型，默认 Micrometer

1.Logging：线程池指标数据会以 Json 格式输出到指定的日志文件里

2.Internal_logging：线程池指标数据会以 Json 格式输出到项目日志文件里

3.Micrometer：采用监控门面，通过引入相关 Micrometer 依赖采集到相应的存储平台里（如 Prometheus，InfluxDb...）

4.暴露 Endpoint端点，可以通过 http 方式实时获取指标数据

:::


### micrometer

引入 prometheus（也可以用其他） 对应依赖，job 配置后 prometheus 管理台看到如下图所示，说明已经开始正常采集指标配置

![采集指标](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/435f0a69790946f8bff7761c40a0a0db~tplv-k3u1fbpfcp-zoom-1.image)

### logging

指标数据以 json 日志格式输出磁盘，地址 ${logPath}/dynamictp/${appName}.monitor.log

  ```bash
  {"datetime": "2022-04-17 11:35:15.208", "app_name": "dynamic-tp-nacos-cloud-demo", "thread_pool_metrics": {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"CallerRunsPolicy","queueCapacity":2000,"fair":false,"queueTimeoutCount":0,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"runTimeoutCount":0,"queueRemainingCapacity":2000,"corePoolSize":4,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dynamic":true,"maximumPoolSize":6,"poolName":"dtpExecutor1"}}
  {"datetime": "2022-04-17 11:35:15.209", "app_name": "dynamic-tp-nacos-cloud-demo", "thread_pool_metrics": {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"CallerRunsPolicy","queueCapacity":2000,"fair":false,"queueTimeoutCount":0,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"runTimeoutCount":0,"queueRemainingCapacity":2000,"corePoolSize":2,"queueType":"TaskQueue","completedTaskCount":0,"dynamic":true,"maximumPoolSize":4,"poolName":"dtpExecutor2"}}
  {"datetime": "2022-04-17 11:35:15.209", "app_name": "dynamic-tp-nacos-cloud-demo", "thread_pool_metrics": {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"queueCapacity":2147483647,"fair":false,"queueTimeoutCount":0,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"runTimeoutCount":0,"queueRemainingCapacity":2147483647,"corePoolSize":1,"queueType":"LinkedBlockingQueue","completedTaskCount":0,"dynamic":false,"maximumPoolSize":1,"poolName":"commonExecutor"}}
  {"datetime": "2022-04-17 11:35:15.209", "app_name": "dynamic-tp-nacos-cloud-demo", "thread_pool_metrics": {"activeCount":0,"queueSize":0,"largestPoolSize":100,"poolSize":100,"queueCapacity":2147483647,"fair":false,"queueTimeoutCount":0,"rejectCount":0,"waitTaskCount":0,"taskCount":177,"runTimeoutCount":0,"queueRemainingCapacity":2147483647,"corePoolSize":100,"queueType":"TaskQueue","completedTaskCount":177,"dynamic":false,"maximumPoolSize":400,"poolName":"tomcatWebServerTp"}}
  ```

### endpoint 

暴露端点(dynamic-tp)，可以通过 http 方式实时请求
  
  ```json
  [
      {
          "pool_name": "remoting-call",
          "core_pool_size": 6,
          "maximum_pool_size": 12,
          "queue_type": "SynchronousQueue",
          "queue_capacity": 0,
          "queue_size": 0,
          "fair": false,
          "queue_remaining_capacity": 0,
          "active_count": 0,
          "task_count": 21760,
          "completed_task_count": 21760,
          "largest_pool_size": 12,
          "pool_size": 6,
          "wait_task_count": 0,
          "reject_count": 124662,
          "reject_handler_name": "CallerRunsPolicy"
      }
  ]
  ```
