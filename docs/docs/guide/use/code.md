---
title: 代码使用
icon: code
order: 1
author: yanhom
date: 2022-06-11
category:
  - 代码使用
tag:
  - 代码使用
sticky: true
star: true
---

- 线程池实例定义
 
  建议直接配置在配置中心，但是如果想后期再添加到配置中心，可以先用@Bean 编码式声明（方便spring依赖注入）

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
                .keepAliveTime(50)
                .timeUnit(TimeUnit.MILLISECONDS)
                .workQueue(QueueTypeEnum.SYNCHRONOUS_QUEUE.getName(), null, false)
                .waitForTasksToCompleteOnShutdown(true)
                .awaitTerminationSeconds(5)
                .buildDynamic();
    }
  }
  ```


- 代码调用

  从DtpRegistry中根据线程池名称获取，或者通过依赖注入方式(推荐，更优雅)

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

- 更详细使用实例请参考`example`工程
