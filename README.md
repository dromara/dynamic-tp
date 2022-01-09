###  轻量级动态线程池 - DynamicTp



***

#### 简介

+ 参考 [美团线程池实践](https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html) ，对Java线程池参数动态化管理，增加监控、报警功能

+ 基于Spring框架，现只支持SpringBoot项目使用，轻量级，依赖配置中心，引入starter即可食用

+ 基于配置中心实现线程池参数动态调整，实时生效；集成主流配置中心，默认支持Nacos、Apollo，同时提供SPI接口可自定义扩展实现

+ 内置通知报警功能，提供多种报警维度（配置变更通知、活性报警、容量阈值报警、拒绝策略触发报警），默认支持企业微信、钉钉报警，同时提供SPI接口可自定义扩展实现

+ 内置简单线程池指标采集功能，支持通过MicroMeter、日志输出、Endpoint三种方式，可自定义扩展

  

***

####  使用

+ maven依赖

  ```xml
    <dependency>
        <groupId>io.github.lyh200</groupId>
        <artifactId>dynamic-tp-spring-cloud-starter</artifactId>
        <version>1.0.0-RELEASE</version>
    </dependency>
  ```



+ 配置文件

  ```yaml
  spring:
    dynamic:
      tp:
        enabled: true
        enabledBanner: true       # 是否开启banner打印，默认true
        enabledCollect: true      # 是否开启监控指标采集，默认false
        monitorInterval: 5        # 监控时间间隔（报警判断、指标采集）
        nacos:                    # nacos配置
          dataId: dynamic-tp-demo-dev.yml
          group: DEFAULT_GROUP
        apollo:                   # apollo配置
          namespace: dynamic-tp-demo-dev.yml
        configType: yml
        platforms:         # 通知报警平台配置
          - platform: wechat
            urlKey: 3a7500-1287-4bd-a798-c5c3d8b69c  # 替换
            receivers: test1,test2  # 接受人企微名称
          - platform: ding
            urlKey: f80dad441fcd655438f4a08dcd6a     # 替换
            secret: SECb5441fa6f375d5b9d21           # 替换，非sign模式可以没有此值
            receivers: 15810119805                   # 钉钉账号手机号          
        executors:         # 动态线程池配置
          - threadPoolName: dynamic-tp-test-1
            corePoolSize: 6
            maximumPoolSize: 8
            queueCapacity: 200
            keepAliveTime: 50
            notifyItems:   # 报警项，不配置自动会配置（变更通知、容量报警、活性报警、拒绝报警）
              - type: capacity
                enabled: true
                threshold: 80
                platforms: [ding,wechat]    # 可选配置，不配置默认拿上层platforms配置的所以平台
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

####  注意细节

1. 配置文件配置的参数会覆盖通过代码生成方式配置的参数
2. 阻塞队列只有VariableLinkedBlockingQueue类型可以修改capacity



***

#### 报警

<a href="https://imgtu.com/i/7FlTDe"><img src="https://s4.ax1x.com/2022/01/09/7FlTDe.png" alt="7FlTDe.png" border="0" width=350px height=600px/></a>              <a href="https://imgtu.com/i/7FlouD"><img src="https://s4.ax1x.com/2022/01/09/7FlouD.md.png" alt="7FlouD.png" border="0" width=350px height=450px/></a>



***

#### 监控日志

+ 通过引入MicroMeter相关依赖采集到支持的平台
+ 指标Json日志输出磁盘，地址：${user.home}/logs/dynamictp/monitor.log