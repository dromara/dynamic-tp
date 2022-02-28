## 基于配置中心的轻量级动态线程池 - DynamicTp

```yml
    |  __ \                            (_) |__   __|
    | |  | |_   _ _ __   __ _ _ __ ___  _  ___| |_ __  
    | |  | | | | | '_ \ / _` | '_ ` _ \| |/ __| | '_ \ 
    | |__| | |_| | | | | (_| | | | | | | | (__| | |_) |
    |_____/ \__, |_| |_|\__,_|_| |_| |_|_|\___|_| .__/ 
             __/ |                              | |    
            |___/                               |_|    
     :: Dynamic Thread Pool :: 
```

***

### 背景

**使用ThreadPoolExecutor过程中你是否有以下痛点呢？**

> 1.代码中创建了一个ThreadPoolExecutor，但是不知道那几个核心参数设置多少比较合适
>
> 2.凭经验设置参数值，上线后发现需要调整，改代码重启服务，非常麻烦
>
> 3.线程池相对开发人员来说是个黑盒，运行情况不能感知到，直到出现问题

如果你有以上痛点，动态可监控线程池（DynamicTp）或许能帮助到你。

如果看过ThreadPoolExecutor的源码，大概可以知道其实它有提供一些set方法，可以在运行时动态去修改相应的值，这些方法有：

```java
public void setCorePoolSize(int corePoolSize);
public void setMaximumPoolSize(int maximumPoolSize);
public void setKeepAliveTime(long time, TimeUnit unit);
public void setThreadFactory(ThreadFactory threadFactory);
public void setRejectedExecutionHandler(RejectedExecutionHandler handler);
```

现在大多数的互联网项目其实都会微服务化部署，有一套自己的服务治理体系，微服务组件中的分布式配置中心扮演的就是动态修改配置，
实时生效的角色。那么我们是否可以结合配置中心来做运行时线程池参数的动态调整呢？答案是肯定的，而且配置中心相对都是高可用的，
使用它也不用过于担心配置推送出现问题这类事儿，而且也能减少研发动态线程池组件的难度和工作量。

**综上，我们总结出以下的背景**

+   广泛性：在Java开发中，想要提高系统性能，线程池已经是一个90%以上的人都会选择使用的基础工具

+   不确定性：项目中可能会创建很多线程池，既有IO密集型的，也有CPU密集型的，但线程池的参数并不好确定；需要有套机制在运行过程中动态去调整参数

+   无感知性，线程池运行过程中的各项指标一般感知不到；需要有套监控报警机制在事前、事中就能让开发人员感知到线程池的运行状况，及时处理

+   高可用性，配置变更需要及时推送到客户端；需要有高可用的配置管理推送服务，配置中心是现在大多数互联网系统都会使用的组件，与之结合可以大幅度减少开发量及接入难度

***

### 简介

我们基于配置中心对线程池ThreadPoolExecutor做一些扩展，实现对运行中线程池参数的动态修改，实时生效；
以及实时监控线程池的运行状态，触发设置的报警策略时报警，报警信息会推送办公平台（钉钉、企微等）。
报警维度包括（队列容量、线程池活性、拒绝触发等）；同时也会定时采集线程池指标数据供监控平台可视化使用。
使我们能时刻感知到线程池的负载，根据情况及时调整，避免出现问题影响线上业务。

```bash
    |  __ \                            (_) |__   __|
    | |  | |_   _ _ __   __ _ _ __ ___  _  ___| |_ __  
    | |  | | | | | '_ \ / _` | '_ ` _ | |/ __| | '_ \ 
    | |__| | |_| | | | | (_| | | | | | | | (__| | |_) |
    |_____/ __, |_| |_|__,_|_| |_| |_|_|___|_| .__/ 
             __/ |                              | |    
            |___/                               |_|    
     :: Dynamic Thread Pool :: 
```


**特性**
+   **参考[美团线程池实践](https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html)，对线程池参数动态化管理，增加监控、报警功能**

+   **基于Spring框架，现只支持SpringBoot项目使用，轻量级，引入starter即可食用**

+   **基于配置中心实现线程池参数动态调整，实时生效；集成主流配置中心，默认支持Nacos、Apollo，
    同时也提供SPI接口可自定义扩展实现**

+   **内置通知报警功能，提供多种报警维度（配置变更通知、活性报警、容量阈值报警、拒绝策略触发报警），
    默认支持企业微信、钉钉报警，同时提供SPI接口可自定义扩展实现**

+   **内置线程池指标采集功能，支持通过MicroMeter、JsonLog日志输出、Endpoint三种方式，可通过SPI接口自定义扩展实现**

+   **集成管理常用第三方组件的线程池，已集成SpringBoot内置WebServer（tomcat、undertow、jetty）的线程池管理**

+   **Builder提供TTL包装功能，生成的线程池支持上下文信息传递** 
***

### 架构设计

**主要分四大模块**

+   配置变更监听模块：

    1.监听特定配置中心的指定配置文件（默认实现Nacos、Apollo）,可通过内部提供的SPI接口扩展其他实现

    2.解析配置文件内容，内置实现yml、properties配置文件的解析，可通过内部提供的SPI接口扩展其他实现

    3.通知线程池管理模块实现刷新

+   线程池管理模块：

    1.服务启动时从配置中心拉取配置信息，生成线程池实例注册到内部线程池注册中心中

    2.监听模块监听到配置变更时，将变更信息传递给管理模块，实现线程池参数的刷新

    3.代码中通过getExecutor()方法根据线程池名称来获取线程池对象实例

+   监控模块：

    实现监控指标采集以及输出，默认提供以下三种方式，也可通过内部提供的SPI接口扩展其他实现

    1.默认实现Json log输出到磁盘

    2.MicroMeter采集，引入MicroMeter相关依赖

    3.暴雷Endpoint端点，可通过http方式访问

+   通知告警模块：

    对接办公平台，实现通告告警功能，默认实现钉钉、企微，可通过内部提供的SPI接口扩展其他实现，通知告警类型如下

    1.线程池参数变更通知

    2.阻塞队列容量达到设置阈值告警

    3.线程池活性达到设置阈值告警

    4.触发拒绝策略告警

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/91ea4c3e1166426e8dca9903dacfd9eb~tplv-k3u1fbpfcp-zoom-1.image)
***

### 使用

+   **maven依赖**
1. apollo应用用接入用此依赖
    ```xml
        <dependency>
            <groupId>io.github.lyh200</groupId>
            <artifactId>dynamic-tp-spring-boot-starter-apollo</artifactId>
            <version>1.0.1</version>
        </dependency>
    ```
2. spring-cloud场景下的nacos应用接入用此依赖
    ```xml
        <dependency>
            <groupId>io.github.lyh200</groupId>
            <artifactId>dynamic-tp-spring-cloud-starter-nacos</artifactId>
            <version>1.0.1</version>
        </dependency>
    ```
3. 非spring-cloud场景下的nacos应用接入用此依赖
    ```xml
        <dependency>
            <groupId>io.github.lyh200</groupId>
            <artifactId>dynamic-tp-spring-boot-starter-nacos</artifactId>
            <version>1.0.1</version>
        </dependency>
    ```

+   线程池配置

    ```yaml
    spring:
      dynamic:
        tp:
          enabled: true
          enabledBanner: true        # 是否开启banner打印，默认true
          enabledCollect: false      # 是否开启监控指标采集，默认false
          collectorType: logging     # 监控数据采集器类型（JsonLog | MicroMeter），默认logging
          logPath: /home/logs        # 监控日志数据路径，默认${user.home}/logs
          monitorInterval: 5         # 监控时间间隔（报警判断、指标采集），默认5s
          nacos:                     # nacos配置，不配置有默认值（规则name-dev.yml这样）
            dataId: dynamic-tp-demo-dev.yml
            group: DEFAULT_GROUP
          apollo:                    # apollo配置，不配置默认拿apollo配置第一个namespace
            namespace: dynamic-tp-demo-dev.yml
          configType: yml            # 配置文件类型
          platforms:                 # 通知报警平台配置
            - platform: wechat
              urlKey: 3a7500-1287-4bd-a798-c5c3d8b69c  # 替换
              receivers: test1,test2                   # 接受人企微名称
            - platform: ding
              urlKey: f80dad441fcd655438f4a08dcd6a     # 替换
              secret: SECb5441fa6f375d5b9d21           # 替换，非sign模式可以没有此值
              receivers: 15810119805                   # 钉钉账号手机号    
          tomcatTp:                                    # tomcat web server线程池配置
              minSpare: 100
              max: 400      
          jettyTp:                                     # jetty web server线程池配置
              min: 100
              max: 400     
          undertowTp:                                  # undertow web server线程池配置
              ioThreads: 100
              workerThreads: 400      
          executors:                                   # 动态线程池配置
            - threadPoolName: dynamic-tp-test-1
              corePoolSize: 6
              maximumPoolSize: 8
              queueCapacity: 200
              queueType: VariableLinkedBlockingQueue   # 任务队列，查看源码QueueTypeEnum枚举类
              rejectedHandlerType: CallerRunsPolicy    # 拒绝策略，查看RejectedTypeEnum枚举类
              keepAliveTime: 50
              allowCoreThreadTimeOut: false
              threadNamePrefix: test           # 线程名前缀
              notifyItems:                     # 报警项，不配置自动会配置（变更通知、容量报警、活性报警、拒绝报警）
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
    ```

+   代码方式生成，服务启动会自动注册

    ```java
    @Configuration
    public class DtpConfig {

       @Bean
       public DtpExecutor demo1Executor() {
           return DtpCreator.createDynamicFast("demo1-executor");
      }

       @Bean
       public ThreadPoolExecutor demo2Executor() {
           return ThreadPoolBuilder.newBuilder()
                  .threadPoolName("demo2-executor")
                  .corePoolSize(8)
                  .maximumPoolSize(16)
                  .keepAliveTime(50)
                  .allowCoreThreadTimeOut(true)
                  .workQueue(QueueTypeEnum.SYNCHRONOUS_QUEUE.getName(), null, false)
                  .rejectedExecutionHandler(RejectedTypeEnum.CALLER_RUNS_POLICY.getName())
                  .buildDynamic();
      }
    }
    ```


+   代码调用，根据线程池名称获取

    ```java
    public static void main(String[] args) {
           DtpExecutor dtpExecutor = DtpRegistry.getExecutor("dynamic-tp-test-1");
           dtpExecutor.execute(() -> System.out.println("test"));
    }
    ```

***

### 注意事项

+  配置文件配置的参数会覆盖通过代码生成方式配置的参数

+  阻塞队列只有VariableLinkedBlockingQueue类型可以修改capacity，该类型功能和LinkedBlockingQueue相似，
   只是capacity不是final类型，可以修改， VariableLinkedBlockingQueue参考RabbitMq的实现

+  启动看到如下日志输出证明接入成功

    ```bash

    |  __ \                            (_) |__   __|   
    | |  | |_   _ _ __   __ _ _ __ ___  _  ___| |_ __  
    | |  | | | | | '_ \ / _` | '_ ` _ | |/ __| | '_ \ 
    | |__| | |_| | | | | (_| | | | | | | | (__| | |_) |
    |_____/ __, |_| |_|__,_|_| |_| |_|_|___|_| .__/ 
             __/ |                              | |    
            |___/                               |_|    
     :: Dynamic Thread Pool :: 

    DynamicTp register, executor: DtpMainPropWrapper(dtpName=dynamic-tp-test-1, corePoolSize=6, maxPoolSize=8, keepAliveTime=50, queueType=VariableLinkedBlockingQueue, queueCapacity=200, rejectType=RejectedCountableCallerRunsPolicy, allowCoreThreadTimeOut=false)
    ```


+  配置变更会推送通知消息，且会高亮变更的字段

    ```bash

    DynamicTp [dynamic-tp-test-1] refresh end, changed keys: [corePoolSize, queueCapacity], corePoolSize: [6 => 4], maxPoolSize: [8 => 8], queueType: [VariableLinkedBlockingQueue => VariableLinkedBlockingQueue], queueCapacity: [200 => 2000], keepAliveTime: [50s => 50s], rejectedType: [CallerRunsPolicy => CallerRunsPolicy], allowsCoreThreadTimeOut: [false => false]
    ```

***
### 通知报警

+ 触发报警阈值会推送相应报警消息（活性、容量、拒绝），且会高亮显示相应字段

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bb4b2d4390b14965b7470b708674ccbe~tplv-k3u1fbpfcp-zoom-1.image)


+ 配置变更会推送通知消息，且会高亮变更的字段

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9fb3e28c1a4e4d46a3ecbf3427181576~tplv-k3u1fbpfcp-zoom-1.image)


***

### 监控日志

![监控数据](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ec5a7d1a31e7418ba5d9a101a5c03826~tplv-k3u1fbpfcp-zoom-1.image)

通过collectType属性配置监控指标采集类型，默认 logging

+   MicroMeter：通过引入相关MicroMeter依赖采集到相应的平台
    （如Prometheus，InfluxDb...）

+   Logging：定时采集指标数据以Json日志格式输出磁盘，地址${logPath}/dy
    namictp/${appName}.monitor.log
    ```bash
    2022-01-11 00:25:20.599 INFO [dtp-monitor-thread-1:d.m.log] {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"RejectedCountableCallerRunsPolicy","queueCapacity":1024,"fair":false,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"queueRemainingCapacity":1024,"corePoolSize":6,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dtpName":"remoting-call","maximumPoolSize":8}
    2022-01-11 00:25:25.603 INFO [dtp-monitor-thread-1:d.m.log] {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"RejectedCountableCallerRunsPolicy","queueCapacity":1024,"fair":false,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"queueRemainingCapacity":1024,"corePoolSize":6,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dtpName":"remoting-call","maximumPoolSize":8}
    2022-01-11 00:25:30.609 INFO [dtp-monitor-thread-1:d.m.log] {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"RejectedCountableCallerRunsPolicy","queueCapacity":1024,"fair":false,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"queueRemainingCapacity":1024,"corePoolSize":6,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dtpName":"remoting-call","maximumPoolSize":8}
    2022-01-11 00:25:35.613 INFO [dtp-monitor-thread-1:d.m.log] {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"RejectedCountableCallerRunsPolicy","queueCapacity":1024,"fair":false,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"queueRemainingCapacity":1024,"corePoolSize":6,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dtpName":"remoting-call","maximumPoolSize":8}
    2022-01-11 00:25:40.616 INFO [dtp-monitor-thread-1:d.m.log] {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"RejectedCountableCallerRunsPolicy","queueCapacity":1024,"fair":false,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"queueRemainingCapacity":1024,"corePoolSize":6,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dtpName":"remoting-call","maximumPoolSize":8}
    ```

+   暴露EndPoint端点(dynamic-tp)，可以通过http方式请求
    ```json
    [
        {
            "dtp_name": "remoting-call",
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
        },
        {
            "max_memory": "228 MB",
            "total_memory": "147 MB",
            "free_memory": "44.07 MB",
            "usable_memory": "125.07 MB"
        }
    ]
    ```

***

### 介绍文章

[https://juejin.cn/post/7063408526894301192](https://juejin.cn/post/7063408526894301192)

[https://juejin.cn/post/7069297636552998943](https://juejin.cn/post/7069581808932749348)

***
### 联系我

对项目有什么想法或者建议，可以加我微信交流，或者创建[issues](https://github.com/lyh200/dynamic-tp-spring-cloud-starter/issues)，一起完善项目

![](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/530709dc29604630b6d1537d7c160ea5~tplv-k3u1fbpfcp-watermark.image)