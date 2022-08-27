---
title: 配置文件
icon: config
order: 1
author: yanhom
date: 2022-06-11
category:
  - 配置文件
tag:
  - 配置文件
sticky: true
star: true
---


::: tip
1.动态线程池配置文件，建议单独开一个文件放到配置中心

2.建议最好使用yml文件配置，可读性、可操作性更友好

3.给出的是全配置项，使用不到的项或者使用默认值的项都可以删除，减少配置项
:::

::: danger
1.下述配置项的值都是随便填写的，请不要直接使用该值，根据自己项目做调整
:::

- 线程池配置（yml 类型）

```yaml
spring:
  dynamic:
    tp:
      enabled: true
      enabledBanner: true           # 是否开启banner打印，默认true
      enabledCollect: true          # 是否开启监控指标采集，默认false
      collectorTypes: micrometer,logging     # 监控数据采集器类型（logging | micrometer | internal_logging），默认micrometer
      logPath: /home/logs           # 监控日志数据路径，默认 ${user.home}/logs，采集类型非logging不用配置
      monitorInterval: 5            # 监控时间间隔（报警判断、指标采集），默认5s
      nacos:                        # nacos配置，不配置有默认值（规则appname-dev.yml这样），cloud应用不需要配置
        dataId: dynamic-tp-demo-dev.yml
        group: DEFAULT_GROUP
      apollo:                       # apollo配置，不配置默认拿apollo配置第一个namespace
        namespace: dynamic-tp-demo-dev.yml
      configType: yml               # 配置文件类型，非cloud nacos 和 apollo需配置，其他不用配
      platforms:                    # 通知报警平台配置
        - platform: wechat
          urlKey: 3a700-127-4bd-a798-c53d8b69c     # 替换
          receivers: test1,test2                   # 接受人企微名称
        - platform: ding
          urlKey: f80dad441fcd655438f4a08dcd6a     # 替换
          secret: SECb5441fa6f375d5b9d21           # 替换，非sign模式可以没有此值
          receivers: 18888888888                   # 钉钉账号手机号
        - platform: lark
          urlKey: 0d944ae7-b24a-40                 # 替换
          receivers: test1,test2                   # 接受人飞书名称/openid
      tomcatTp:                                    # tomcat webserver线程池配置
        corePoolSize: 100
        maximumPoolSize: 200
        keepAliveTime: 60
      jettyTp:                                     # jetty weberver线程池配置
        corePoolSize: 100
        maximumPoolSize: 200
      undertowTp:                                  # undertow webserver线程池配置
        corePoolSize: 100
        maximumPoolSize: 200
        keepAliveTime: 60
      hystrixTp:                                   # hystrix 线程池配置
        - threadPoolName: hystrix1
          corePoolSize: 100
          maximumPoolSize: 200
          keepAliveTime: 60
      dubboTp:                                     # dubbo 线程池配置
        - threadPoolName: dubboTp#20880            # 名称规则：dubboTp + "#" + 协议端口
          theadPoolAliasName: 测试线程池             # dubbo线程池
          corePoolSize: 100
          maximumPoolSize: 200
          keepAliveTime: 60
          notifyItems:                             # 报警项，不配置自动会按默认值配置（变更通知、容量报警、活性报警）
            - type: capacity                       # 报警项类型，查看源码 NotifyTypeEnum枚举类
              enabled: true
              threshold: 80                        # 报警阈值
              platforms: [ding,wechat]             # 可选配置，不配置默认拿上层platforms配置的所以平台
              interval: 120                        # 报警间隔（单位：s）
      rocketMqTp:                                  # rocketmq 线程池配置
        - threadPoolName: group1#topic1            # 名称规则：group + "#" + topic
          corePoolSize: 200
          maximumPoolSize: 200
          keepAliveTime: 60
      executors:                                   # 动态线程池配置，都有默认值，采用默认值的可以不配置该项，减少配置量
        - threadPoolName: dtpExecutor1
          theadPoolAliasName: 测试线程池             # 线程池别名
          executorType: common                     # 线程池类型common、eager：适用于io密集型
          corePoolSize: 6
          maximumPoolSize: 8
          queueCapacity: 200
          queueType: VariableLinkedBlockingQueue   # 任务队列，查看源码QueueTypeEnum枚举类
          rejectedHandlerType: CallerRunsPolicy    # 拒绝策略，查看RejectedTypeEnum枚举类
          keepAliveTime: 50
          allowCoreThreadTimeOut: false                  # 是否允许核心线程池超时
          threadNamePrefix: test                         # 线程名前缀
          waitForTasksToCompleteOnShutdown: false        # 参考spring线程池设计，优雅关闭线程池
          awaitTerminationSeconds: 5                     # 单位（s）
          preStartAllCoreThreads: false                  # 是否预热所有核心线程，默认false
          runTimeout: 200                                # 任务执行超时阈值，目前只做告警用，单位（ms）
          queueTimeout: 100                              # 任务在队列等待超时阈值，目前只做告警用，单位（ms）
          taskWrapperNames: ["ttl"]                      # 任务包装器名称，集成TaskWrapper接口
          notifyItems:                     # 报警项，不配置自动会按默认值配置（变更通知、容量报警、活性报警、拒绝报警、任务超时报警）
            - type: capacity               # 报警项类型，查看源码 NotifyTypeEnum枚举类
              enabled: true
              threshold: 80                # 报警阈值
              platforms: [ding,wechat]     # 可选配置，不配置默认拿上层platforms配置的所以平台
              interval: 120                # 报警间隔（单位：s）
            - type: change
              enabled: true
            - type: liveness
              enabled: true
              threshold: 80
            - type: reject
              enabled: true
              threshold: 1
            - type: run_timeout
              enabled: true
              threshold: 1
            - type: queue_timeout
              enabled: true
              threshold: 1
```

- 线程池配置（properties 类型）

  ```properties
      spring.dynamic.tp.enabled=true
      spring.dynamic.tp.enabledBanner=true
      spring.dynamic.tp.enabledCollect=true
      spring.dynamic.tp.collectorType=logging
      spring.dynamic.tp.monitorInterval=5
      spring.dynamic.tp.executors[0].threadPoolName=dynamic-tp-test-1
      spring.dynamic.tp.executors[0].corePoolSize=50
      spring.dynamic.tp.executors[0].maximumPoolSize=50
      spring.dynamic.tp.executors[0].queueCapacity=3000
      spring.dynamic.tp.executors[0].queueType=VariableLinkedBlockingQueue
      spring.dynamic.tp.executors[0].rejectedHandlerType=CallerRunsPolicy
      spring.dynamic.tp.executors[0].keepAliveTime=50
      spring.dynamic.tp.executors[0].allowCoreThreadTimeOut=false
      spring.dynamic.tp.executors[0].threadNamePrefix=test1
      spring.dynamic.tp.executors[0].notifyItems[0].type=capacity
      spring.dynamic.tp.executors[0].notifyItems[0].enabled=false
      spring.dynamic.tp.executors[0].notifyItems[0].threshold=80
      spring.dynamic.tp.executors[0].notifyItems[0].platforms[0]=ding
      spring.dynamic.tp.executors[0].notifyItems[0].platforms[1]=wechat
      spring.dynamic.tp.executors[0].notifyItems[0].interval=120
      spring.dynamic.tp.executors[0].notifyItems[1].type=change
      spring.dynamic.tp.executors[0].notifyItems[1].enabled=false
      spring.dynamic.tp.executors[0].notifyItems[2].type=liveness
      spring.dynamic.tp.executors[0].notifyItems[2].enabled=false
      spring.dynamic.tp.executors[0].notifyItems[2].threshold=80
      spring.dynamic.tp.executors[0].notifyItems[3].type=reject
      spring.dynamic.tp.executors[0].notifyItems[3].enabled=false
      spring.dynamic.tp.executors[0].notifyItems[3].threshold=1
      spring.dynamic.tp.executors[1].threadPoolName=dynamic-tp-test-2
      spring.dynamic.tp.executors[1].corePoolSize=20
      spring.dynamic.tp.executors[1].maximumPoolSize=30
      spring.dynamic.tp.executors[1].queueCapacity=1000
      spring.dynamic.tp.executors[1].queueType=VariableLinkedBlockingQueue
      spring.dynamic.tp.executors[1].rejectedHandlerType=CallerRunsPolicy
      spring.dynamic.tp.executors[1].keepAliveTime=50
      spring.dynamic.tp.executors[1].allowCoreThreadTimeOut=false
      spring.dynamic.tp.executors[1].threadNamePrefix=test2
  ```

- 线程池配置（json 类型）
```
{
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
```

- 线程池配置（用于zk工具一键导入）

```
/configserver/dev/dynamic-tp-cloud-zookeeper-demo,dev=spring.dynamic.tp.enabled=true
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
```