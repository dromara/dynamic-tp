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

+ 广泛性，在Java开发中，想要提高系统性能，线程池已经是一个90%以上的开发人员都会选择使用的基础工具

+ 不确定性，项目中可能会创建很多线程池，既有IO密集型的，也有CPU密集型的，但线程池的参数并不好确定；需要有套机制在运行过程中动态去调整参数
> 1. 最大线程数和任务队列设置过小，会导致服务大量抛出RejectedExecutionException异常
> 2. 最大线程数设置过小，任务队列大小设置过大，会导致任务积压，响应时长变长
> 3. 最大线程数设置过大，会导致线程切换频繁，处理速度反而下降
> 4. 核心线程数设置过大，会导致空闲线程太多，浪费系统资源

+ 无感知性，线程池运行过程中的各项指标一般感知不到；需要有套监控报警机制在事前、事中就能让开发人员感知到线程池的运行状况，及时处理

+ 不便性，修改线程池参数后需要重启应用，灰常不方便 

+ 高可用性，配置变更需要及时推送到客户端；需要有高可用的配置管理推送服务，分布式配置中心是现在大多数互联网系统都会使用的组件，与之结合可以大幅度减少开发量及接入难度



***

### 简介

+ 参考 [美团线程池实践](https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html) ，对Java线程池参数动态化管理，增加监控、报警功能

+ 基于Spring框架，现只支持SpringBoot项目使用，轻量级，依赖配置中心，引入starter即可食用

+ 基于配置中心实现线程池参数动态调整，实时生效；集成主流配置中心，默认支持Nacos、Apollo，同时提供SPI接口可自定义扩展实现

+ 内置通知报警功能，提供多种报警维度（配置变更通知、活性报警、容量阈值报警、拒绝策略触发报警），默认支持企业微信、钉钉报警，同时提供SPI接口可自定义扩展实现

+ 内置简单线程池指标采集功能，支持通过MicroMeter收集、日志输出、Endpoint三种方式，可自定义扩展



***

### 架构设计

+ 功能主要分四大模块

+ 配置变更监听模块：监听配置中心的变化，实时更新线程池参数，默认实现Nacos、Apollo监听，可通过内部提供的SPI接口扩展其他实现

+ 线程池管理模块：主要实现动态线程池的增删查改功能

+ 监控模块：实现监控指标采集以及输出，默认实现JsonLog输出、MicroMeter采集、Endpoint暴露端点三种方式，可通过内部提供的SPI接口扩展其他实现

+ 通知告警模块：对接办公平台，实现告警信息通知，默认实现钉钉、企微，可通过内部提供的SPI接口扩展其他实现</br></br>
<img src="https://s4.ax1x.com/2022/01/09/7FoNZV.png" alt="7FoNZV.png" border="0" width=550px height=450px/>                                                      



***

### 使用

+ maven依赖

  ```xml
    <dependency>
        <groupId>io.github.lyh200</groupId>
        <artifactId>dynamic-tp-spring-cloud-starter</artifactId>
        <version>1.0.2-RELEASE</version>
    </dependency>
  ```



+ 配置文件

  ```yaml
  spring:
    dynamic:
      tp:
        enabled: true
        enabledBanner: true        # 是否开启banner打印，默认true
        enabledCollect: false      # 是否开启监控指标采集，默认false
        collectorType: logging     # 监控数据采集器类型（JsonLog | MicroMeter），默认logging
        logPath: /home             # 监控日志数据路径，默认${user.home}/logs
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



+ 代码生成
  
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

  

+ 代码调用

  ```java
   public static void main(String[] args) {
          DtpExecutor dtpExecutor = DtpRegistry.getExecutor("dynamic-tp-test-1");
          dtpExecutor.execute(() -> System.out.println("test"));
   }
  ```



***

### 注意事项

1. 配置项都有默认值，使用时可以查看源码，使用默认值的可以不配置相应字段

2. 配置文件配置的参数会覆盖通过代码生成方式配置的参数

3. 阻塞队列只有VariableLinkedBlockingQueue类型可以修改capacity

4. ```yaml
   启动看到如下日志输出证明接入成功
   
   |  __ \                            (_) |__   __|   
   | |  | |_   _ _ __   __ _ _ __ ___  _  ___| |_ __  
   | |  | | | | | '_ \ / _` | '_ ` _ \| |/ __| | '_ \ 
   | |__| | |_| | | | | (_| | | | | | | | (__| | |_) |
   |_____/ \__, |_| |_|\__,_|_| |_| |_|_|\___|_| .__/ 
            __/ |                              | |    
           |___/                               |_|    
    :: Dynamic Thread Pool :: 
   
   DynamicTp register, executor: DtpMainPropWrapper(dtpName=dynamic-tp-test-1, corePoolSize=6, maxPoolSize=8, keepAliveTime=50, queueType=VariableLinkedBlockingQueue, queueCapacity=200, rejectType=RejectedCountableCallerRunsPolicy, allowCoreThreadTimeOut=false)
   ```

5. ```yml
   配置变更会输出相应日志，以及会推送通知
   
   DynamicTp [dynamic-tp-test-1] refresh end, changed keys: [corePoolSize, queueCapacity], corePoolSize: [6 => 4], maxPoolSize: [8 => 8], queueType: [VariableLinkedBlockingQueue => VariableLinkedBlockingQueue], queueCapacity: [200 => 2000], keepAliveTime: [50s => 50s], rejectedType: [CallerRunsPolicy => CallerRunsPolicy], allowsCoreThreadTimeOut: [false => false]
   ```



***

### 通知报警
+ 触发报警阈值会推送相应报警消息（活性、容量、拒绝策略），且会高亮显示相应字段</br></br>
<img src="https://s4.ax1x.com/2022/01/09/7FlTDe.png" alt="7FlTDe.png" border="0" width=350px height=500px/>      


+ 配置变更会推送通知消息，且会高亮变更的字段</br></br>
<img src="https://s4.ax1x.com/2022/01/09/7FlouD.md.png" alt="7FlouD.png" border="0" width=350px height=450px/>



***

### 监控日志

+ 通过collectType属性配置监控指标输出类型，默认 logging

+ MicroMeter：通过引入相关MicroMeter依赖采集到相应的平台（如Prometheus，InfluxDb...）

+ Logging：定时采集指标数据以Json日志格式输出磁盘，地址：${logPath}/dynamictp/${appName}.monitor.log

+ 也有提供EndPoint端点(dynamic-tp)，可以通过http方式请求

```
2022-01-11 00:25:20.599 INFO [dtp-monitor-thread-1:d.m.log] {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"RejectedCountableCallerRunsPolicy","queueCapacity":1024,"fair":false,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"queueRemainingCapacity":1024,"corePoolSize":6,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dtpName":"remoting-call","maximumPoolSize":8}
2022-01-11 00:25:25.603 INFO [dtp-monitor-thread-1:d.m.log] {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"RejectedCountableCallerRunsPolicy","queueCapacity":1024,"fair":false,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"queueRemainingCapacity":1024,"corePoolSize":6,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dtpName":"remoting-call","maximumPoolSize":8}
2022-01-11 00:25:30.609 INFO [dtp-monitor-thread-1:d.m.log] {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"RejectedCountableCallerRunsPolicy","queueCapacity":1024,"fair":false,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"queueRemainingCapacity":1024,"corePoolSize":6,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dtpName":"remoting-call","maximumPoolSize":8}
2022-01-11 00:25:35.613 INFO [dtp-monitor-thread-1:d.m.log] {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"RejectedCountableCallerRunsPolicy","queueCapacity":1024,"fair":false,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"queueRemainingCapacity":1024,"corePoolSize":6,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dtpName":"remoting-call","maximumPoolSize":8}
2022-01-11 00:25:40.616 INFO [dtp-monitor-thread-1:d.m.log] {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"RejectedCountableCallerRunsPolicy","queueCapacity":1024,"fair":false,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"queueRemainingCapacity":1024,"corePoolSize":6,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dtpName":"remoting-call","maximumPoolSize":8}
```



***

### 联系我

+ 对项目有什么想法或者建议，可以加我vx交流，或者创建[issues](https://github.com/lyh200/dynamic-tp-spring-cloud-starter/issues)，一起完善项目

<img src="https://s4.ax1x.com/2022/01/09/7Fy2an.jpg" alt="7Fy2an.jpg" border="0" width=250px height=250px/>

