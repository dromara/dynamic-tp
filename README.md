<p align="center">
	<img alt="logo" src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3a70070ebc4546aba734e3ba97e7b193~tplv-k3u1fbpfcp-zoom-1.image" width="50%">
</p>
<p align="center">
	<strong>基于配置中心的轻量级动态线程池，内置监控告警功能，可通过SPI自定义扩展实现</strong>
</p>

<p align="center">
	<a href="https://gitee.com/dromara/dynamic-tp"><img src="https://gitee.com/dromara/dynamic-tp/badge/star.svg"></a>
	<a href="https://github.com/dromara/dynamic-tp"><img src="https://img.shields.io/github/stars/dromara/dynamic-tp?style=flat-square&logo=github"></a>
  	<a href="https://github.com/dromara/dynamic-tp/blob/master/LICENSE"><img src="https://img.shields.io/github/license/dromara/dynamic-tp.svg?style=flat-square"></a>
    <a target="_blank" href="https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/530709dc29604630b6d1537d7c160ea5~tplv-k3u1fbpfcp-watermark.image"><img src='https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ddfaed2cce2a47608fb0c0c375a10f08~tplv-k3u1fbpfcp-zoom-1.image' alt='备注加群'></a>
</p>

---

## 背景

**使用 ThreadPoolExecutor 过程中你是否有以下痛点呢？**

> 1.代码中创建了一个 ThreadPoolExecutor，但是不知道那几个核心参数设置多少比较合适
>
> 2.凭经验设置参数值，上线后发现需要调整，改代码重启服务，非常麻烦
>
> 3.线程池相对开发人员来说是个黑盒，运行情况不能感知到，直到出现问题

如果你有以上痛点，动态可监控线程池（DynamicTp）或许能帮助到你。

如果看过 ThreadPoolExecutor 的源码，大概可以知道其实它有提供一些 set 方法，可以在运行时动态去修改相应的值，这些方法有：

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

**综上，可以总结出以下的背景**

- 广泛性：在 Java 开发中，想要提高系统性能，线程池已经是一个 90%以上的人都会选择使用的基础工具

- 不确定性：项目中可能会创建很多线程池，既有 IO 密集型的，也有 CPU 密集型的，但线程池的参数并不好确定；需要有套机制在运行过程中动态去调整参数

- 无感知性，线程池运行过程中的各项指标一般感知不到；需要有套监控报警机制在事前、事中就能让开发人员感知到线程池的运行状况，及时处理

- 高可用性，配置变更需要及时推送到客户端；需要有高可用的配置管理推送服务，配置中心是现在大多数互联网系统都会使用的组件，与之结合可以大幅度减少开发量及接入难度

---

## 简介

我们基于配置中心对线程池 ThreadPoolExecutor 做一些扩展，实现对运行中线程池参数的动态修改，实时生效；
以及实时监控线程池的运行状态，触发设置的报警策略时报警，报警信息会推送办公平台（钉钉、企微等）。
报警维度包括（队列容量、线程池活性、拒绝触发、任务执行等待超时等）；同时也会定时采集线程池指标数据供监控平台可视化使用。
使我们能时刻感知到线程池的负载，根据情况及时调整，避免出现问题影响线上业务。

**特性**

- **参考[美团线程池实践](https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html)，对线程池参数动态化管理，增加监控、报警等增强功能**

- **基于 Spring 框架，现只支持 SpringBoot 项目使用，轻量级，引入 starter 即可使用**

- **基于配置中心实现线程池参数动态调整，实时生效；集成主流配置中心，已支持 Nacos、Apollo、Zookeeper、Consul，
  同时也提供 SPI 接口可自定义扩展实现**

- **内置通知报警功能，提供多种报警维度（配置变更通知、活性报警、容量阈值报警、拒绝触发报警、任务执行或等待超时报警），
  默认支持企业微信、钉钉报警，同时提供 SPI 接口可自定义扩展实现**

- **内置线程池指标采集功能，支持通过 MicroMeter、JsonLog 日志输出、Endpoint 三种方式，可通过 SPI 接口自定义扩展实现**

- **集成管理常用第三方组件的线程池，已集成 SpringBoot 内置 WebServer（Tomcat、Undertow、Jetty）的线程池管理**

- **提供任务包装功能，实现TaskWrapper接口即可，如TtlTaskWrapper可以支持线程池上下文信息传递**

- **JUC普通线程池也可以被框架监控（@DynamicTp）**

- **参考Tomcat线程池提供了io密集型场景使用的EagerDtpExecutor**

---

## 架构设计

**主要分四大模块**

- 配置变更监听模块：

  1.监听特定配置中心的指定配置文件（已实现 Nacos、Apollo、Zookeeper、Consul），可通过内部提供的SPI接口扩展其他实现

  2.解析配置文件内容，内置实现 yml、properties 配置文件的解析，可通过内部提供的 SPI 接口扩展其他实现

  3.通知线程池管理模块实现刷新

- 线程池管理模块：

  1.服务启动时从配置中心拉取配置，生成线程池实例注册到内部线程池注册中心以及Spring容器中

  2.监听模块监听到配置变更时，将变更信息传递给管理模块，实现线程池参数的刷新

  3.代码中通过依赖注入（推荐）或者getExecutor()方法根据线程池名称来获取线程池实例

- 监控模块：

  实现监控指标采集以及输出，默认提供以下三种方式，也可通过内部提供的 SPI 接口扩展其他实现

  1.默认实现JsonLog输出到磁盘，可以自己采集解析日志，存储展示

  2.MicroMeter采集，引入 MicroMeter 相关依赖，暴露相关端点

  3.暴雷自定义Endpoint端点，可通过 http 方式实时访问

- 通知告警模块：

  对接办公平台，实现通知告警功能，默认实现钉钉、企微，可通过内部提供的 SPI 接口扩展其他实现，通知告警类型如下

  1.线程池主要参数变更通知

  2.阻塞队列容量达到设置的告警阈值

  3.线程池活性达到设置的告警阈值

  4.触发拒绝策略告警，格式：A/B，A：该报警项前后两次报警区间累加数量，B：该报警项累计总数
  
  5.任务执行超时告警，格式：A/B，A：该报警项前后两次报警区间累加数量，B：该报警项累计总数
  
  6.任务等待超时告警，格式：A/B，A：该报警项前后两次报警区间累加数量，B：该报警项累计总数

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/91ea4c3e1166426e8dca9903dacfd9eb~tplv-k3u1fbpfcp-zoom-1.image)

---

## 使用

1.引入对应配置中心的依赖

2.配置中心配置线程池实例，配置参考下文（给出的是全配置项，配置项都有默认值）

3.启动类加@EnableDynamicTp注解

4.使用@Resource或@Autowired注解注入，或通过DtpRegistry.getExecutor("name")获取

5.普通JUC线程池想要被监控，可以@Bean定义时加@DynamicTp注解

6.tips：动态线程池实例服务启动时会根据配置中心的配置动态注册到Spring容器中，建议不要用@Bean编程式重复声明同一线程池实例，直接配置在配置中心就行

7.详细参考下文及Example示例

- **maven 依赖**

1. apollo 应用用接入用此依赖
   ```xml
       <dependency>
           <groupId>io.github.lyh200</groupId>
           <artifactId>dynamic-tp-spring-boot-starter-apollo</artifactId>
           <version>1.0.4</version>
       </dependency>
   ```
2. spring-cloud 场景下的 nacos 应用接入用此依赖
   ```xml
       <dependency>
           <groupId>io.github.lyh200</groupId>
           <artifactId>dynamic-tp-spring-cloud-starter-nacos</artifactId>
           <version>1.0.4</version>
       </dependency>
   ```
3. 非 spring-cloud 场景下的 nacos 应用接入用此依赖
   ```xml
       <dependency>
           <groupId>io.github.lyh200</groupId>
           <artifactId>dynamic-tp-spring-boot-starter-nacos</artifactId>
           <version>1.0.4</version>
       </dependency>
   ```
4. zookeeper 配置中心应用接入
   ```xml
       <dependency>
           <groupId>io.github.lyh200</groupId>
           <artifactId>dynamic-tp-spring-boot-starter-zookeeper</artifactId>
           <version>1.0.4</version>
       </dependency>
   ```
   application.yml 需配置 zk 地址节点信息

    ```yaml
        spring:
          application:
            name: dynamic-tp-zookeeper-demo
          dynamic:
            tp:
              config-type: properties         # zookeeper只支持properties配置
              zookeeper:
                config-version: 1.0.0
                zk-connect-str: 127.0.0.1:2181
                root-node: /configserver/dev
                node: dynamic-tp-zookeeper-demo
    ```
   
5. spring-cloud-starter-zookeeper-config 应用接入
   ```xml
       <dependency>
           <groupId>io.github.lyh200</groupId>
           <artifactId>dynamic-tp-spring-cloud-starter-zookeeper</artifactId>
           <version>1.0.4</version>
       </dependency>
   ```
   
   注：配置中心配置文件参考example-zookeeper-cloud/resource下的config.txt，该文件可以通过`ZKUI`工具导入到`Zookeeper`

6. spring-cloud-starter-consul-config 应用接入
   ```xml
       <dependency>
           <groupId>io.github.lyh200</groupId>
           <artifactId>dynamic-tp-spring-cloud-starter-consul</artifactId>
           <version>1.0.4</version>
       </dependency>
   ```

   注：配置中心配置文件参考example-consul-cloud/resource下的dynamic-tp-cloud-consul-demo-dtp.yml

- 线程池配置（yml 类型）

  ```yaml
  spring:
    dynamic:
      tp:
        enabled: true
        enabledBanner: true        # 是否开启banner打印，默认true
        enabledCollect: false      # 是否开启监控指标采集，默认false
        collectorType: logging     # 监控数据采集器类型（JsonLog | MicroMeter），默认logging
        logPath: /home/logs        # 监控日志数据路径，默认 ${user.home}/logs
        monitorInterval: 5         # 监控时间间隔（报警判断、指标采集），默认5s
        nacos:                     # nacos配置，不配置有默认值（规则name-dev.yml这样），cloud应用不需要配置
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
            coreWorkerThreads: 100                   # 核心线程数
            maxWorkerThreads: 400                    # 最大线程数
            workerKeepAlive: 40                     
        executors:                                   # 动态线程池配置，都有默认值，采用默认值的可以不配置该项，减少配置量
          - threadPoolName: dtpExecutor1
            executorType: common                     # 线程池类型common、eager：适用于io密集型
            corePoolSize: 6
            maximumPoolSize: 8
            queueCapacity: 200
            queueType: VariableLinkedBlockingQueue   # 任务队列，查看源码QueueTypeEnum枚举类
            rejectedHandlerType: CallerRunsPolicy    # 拒绝策略，查看RejectedTypeEnum枚举类
            keepAliveTime: 50
            allowCoreThreadTimeOut: false
            threadNamePrefix: test                         # 线程名前缀
            waitForTasksToCompleteOnShutdown: false        # 参考spring线程池设计
            awaitTerminationSeconds: 5                     # 单位（s）
            preStartAllCoreThreads: false                  # 是否预热核心线程，默认false
            runTimeout: 200                                # 任务执行超时阈值，目前只做告警用，单位（ms）
            queueTimeout: 100                              # 任务在队列等待超时阈值，目前只做告警用，单位（ms）
            taskWrapperNames: ["ttl"]                          # 任务包装器名称，集成TaskWrapper接口
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

- 线程池配置（properties 类型），具体请看example-zookeeper项目下的配置文件

- 定义线程池Bean（可选），建议直接配置在配置中心；但是如果想后期再添加到配置中心，可以先用@Bean声明（方便依赖注入）

  ```java
  @Configuration
  public class DtpConfig {  
    
    /**
     * 通过{@link DynamicTp} 注解定义普通juc线程池，会享受到该框架监控功能，注解名称优先级高于方法名
     *
     * @return 线程池实例
     */
    @DynamicTp("commonExecutor")
    @Bean
    public ThreadPoolExecutor commonExecutor() {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

    /**
     * 通过{@link ThreadPoolCreator} 快速创建一些简单配置的动态线程池
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     *
     * @return 线程池实例
     */
    @Bean
    public DtpExecutor dtpExecutor1() {
        return ThreadPoolCreator.createDynamicFast("dtpExecutor1");
    }

    /**
     * 通过{@link ThreadPoolBuilder} 设置详细参数创建动态线程池（推荐方式），
     * ioIntensive，参考tomcat线程池设计，实现了处理io密集型任务的线程池，具体参数可以看代码注释
     *
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
    @Bean
    public DtpExecutor ioIntensiveExecutor() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName("ioIntensiveExecutor")
                .corePoolSize(20)
                .maximumPoolSize(50)
                .queueCapacity(2048)
                .ioIntensive(true)
                .buildDynamic();
    }

    /**
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
    @Bean
    public ThreadPoolExecutor dtpExecutor2() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName("dtpExecutor2")
                .corePoolSize(10)
                .maximumPoolSize(15)
                .keepAliveTime(15000)
                .timeUnit(TimeUnit.MILLISECONDS)
                .workQueue(QueueTypeEnum.SYNCHRONOUS_QUEUE.getName(), null, false)
                .waitForTasksToCompleteOnShutdown(true)
                .awaitTerminationSeconds(5)
                .buildDynamic();
    }
  }
  ```

* 代码调用，从DtpRegistry中根据线程池名称获取，或者通过依赖注入方式(推荐，更优雅)

  1）依赖注入方式使用，优先推荐依赖注入方式，不能使用依赖注入的场景可以使用方式2
  
  ```java
  @Resource
  private ThreadPoolExecutor dtpExecutor1;
  
  public void exec() {
     dtpExecutor1.execute(() -> System.out.println("test"));
  }
  ```
  
  2）通过DtpRegistry注册器获取
  
  ```java
  public static void main(String[] args) {
     DtpExecutor dtpExecutor = DtpRegistry.getDtpExecutor("dtpExecutor1");
     dtpExecutor.execute(() -> System.out.println("test"));
  }
  ```

* 更详细使用实例请参考`example`工程

---

## 注意事项

- 服务启动时会根据配置中心配置的executors动态生成线程池实例注册到spring容器中，动态线程池建议直接配置在配置中心中， 
  同一线程池实例不要用@Bean编程式重复配置，虽然会覆盖掉

- 阻塞队列只有 VariableLinkedBlockingQueue 类型可以修改 capacity，该类型功能和 LinkedBlockingQueue 相似，
  只是 capacity 不是 final 类型，可以修改， VariableLinkedBlockingQueue 参考 RabbitMq 的实现

- 启动看到如下日志输出证明接入成功

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
  ```

* 配置变更会推送通知消息，且会高亮变更的字段

  ```bash
  DynamicTp refresh, name: [dtpExecutor2], changed keys: [corePoolSize, queueCapacity], corePoolSize: [6 => 4], maxPoolSize: [8 => 8], queueType: [VariableLinkedBlockingQueue => VariableLinkedBlockingQueue], queueCapacity: [200 => 2000], keepAliveTime: [50s => 50s], rejectedType: [CallerRunsPolicy => CallerRunsPolicy], allowsCoreThreadTimeOut: [false => false]
  ```

---

## 通知报警

- 触发报警阈值会推送相应报警消息（活性、容量、拒绝、任务等待超时、任务执行超时），且会高亮显示相应字段

![告警](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d65151e3e9ca460eac18f30ea6be05d3~tplv-k3u1fbpfcp-zoom-1.image)


- 配置变更会推送通知消息，且会高亮变更的字段

![变更通知](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/30093a6ede914887bb5566139352fb8b~tplv-k3u1fbpfcp-zoom-1.image)


---

## 监控

![监控数据](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ec5a7d1a31e7418ba5d9a101a5c03826~tplv-k3u1fbpfcp-zoom-1.image)

通过 collectType 属性配置监控指标采集类型，默认 logging

- MicroMeter：通过引入相关 MicroMeter 依赖采集到相应的平台
  （如 Prometheus，InfluxDb...）

- Logging：定时采集指标数据以 Json 日志格式输出磁盘，地址 ${logPath}/dy
    namictp/${appName}.monitor.log

  ```bash
  {"datetime": "2022-04-17 11:35:15.208", "app_name": "dynamic-tp-nacos-cloud-demo", "thread_pool_metrics": {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"CallerRunsPolicy","queueCapacity":2000,"fair":false,"queueTimeoutCount":0,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"runTimeoutCount":0,"queueRemainingCapacity":2000,"corePoolSize":4,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dynamic":true,"maximumPoolSize":6,"poolName":"dtpExecutor1"}}
  {"datetime": "2022-04-17 11:35:15.209", "app_name": "dynamic-tp-nacos-cloud-demo", "thread_pool_metrics": {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"CallerRunsPolicy","queueCapacity":2000,"fair":false,"queueTimeoutCount":0,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"runTimeoutCount":0,"queueRemainingCapacity":2000,"corePoolSize":2,"queueType":"TaskQueue","completedTaskCount":0,"dynamic":true,"maximumPoolSize":4,"poolName":"dtpExecutor2"}}
  {"datetime": "2022-04-17 11:35:15.209", "app_name": "dynamic-tp-nacos-cloud-demo", "thread_pool_metrics": {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"queueCapacity":2147483647,"fair":false,"queueTimeoutCount":0,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"runTimeoutCount":0,"queueRemainingCapacity":2147483647,"corePoolSize":1,"queueType":"LinkedBlockingQueue","completedTaskCount":0,"dynamic":false,"maximumPoolSize":1,"poolName":"commonExecutor"}}
  {"datetime": "2022-04-17 11:35:15.209", "app_name": "dynamic-tp-nacos-cloud-demo", "thread_pool_metrics": {"activeCount":0,"queueSize":0,"largestPoolSize":100,"poolSize":100,"queueCapacity":2147483647,"fair":false,"queueTimeoutCount":0,"rejectCount":0,"waitTaskCount":0,"taskCount":177,"runTimeoutCount":0,"queueRemainingCapacity":2147483647,"corePoolSize":100,"queueType":"TaskQueue","completedTaskCount":177,"dynamic":false,"maximumPoolSize":400,"poolName":"tomcatWebServerTp"}}
  ```

- 暴露 EndPoint 端点(dynamic-tp)，可以通过 http 方式请求
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

---

## 介绍文章

[美团动态线程池实践思路，开源了](https://juejin.cn/post/7063408526894301192)

[动态线程池框架（DynamicTp），监控及源码解析篇](https://juejin.cn/post/7069581808932749348)

[动态线程池（DynamicTp），动态调整Tomcat、Jetty、Undertow线程池参数篇](https://juejin.cn/post/7073286368629096485)

[美团动态线程池实践思路开源项目（DynamicTp），线程池源码解析及通知告警篇](https://juejin.cn/post/7083387245712900126)

---

## 联系我

对项目有什么想法或者建议，可以**加我微信拉交流群**，或者创建[issues](https://github.com/lyh200/dynamic-tpp/issues)，一起完善项目

![](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/530709dc29604630b6d1537d7c160ea5~tplv-k3u1fbpfcp-watermark.image)