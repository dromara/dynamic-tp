---
title: 工具
icon: tool
order: 1
author: yanhom
date: 2022-08-15
category:
  - 工具
tag:
  - 工具
sticky: true
star: true
---


### 快速创建内存安全线程池

core 模块 ThreadPoolCreator 类提供快速创建内存安全线程池的静态方法，可以用来替换 Executors 类，
内部是基于 ThreadPoolBuilder 来创建的，也可以通过 ThreadPoolBuilder 来创建线程池对象

```java

public static ThreadPoolExecutor createCommonFast(String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadFactory(threadPrefix)
                .buildCommon();
    }

    public static ExecutorService createCommonWithTtl(String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .dynamic(false)
                .threadFactory(threadPrefix)
                .buildWithTtl();
    }

    public static DtpExecutor createDynamicFast(String poolName) {
        return createDynamicFast(poolName, poolName);
    }

    public static DtpExecutor createDynamicFast(String poolName, String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(poolName)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    public static ExecutorService createDynamicWithTtl(String poolName) {
        return createDynamicWithTtl(poolName, poolName);
    }

    public static ExecutorService createDynamicWithTtl(String poolName, String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(poolName)
                .threadFactory(threadPrefix)
                .buildWithTtl();
    }

    public static ThreadPoolExecutor newSingleThreadPool(String threadPrefix, int queueCapacity) {
        return newFixedThreadPool(threadPrefix, 1, queueCapacity);
    }

    public static ThreadPoolExecutor newFixedThreadPool(String threadPrefix, int poolSize, int queueCapacity) {
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(poolSize)
                .maximumPoolSize(poolSize)
                .workQueue(QueueTypeEnum.MEMORY_SAFE_LINKED_BLOCKING_QUEUE.getName(), queueCapacity, null)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    public static ExecutorService newCachedThreadPool(String threadPrefix, int maximumPoolSize) {
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(0)
                .maximumPoolSize(maximumPoolSize)
                .workQueue(QueueTypeEnum.SYNCHRONOUS_QUEUE.getName(), null, null)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    public static ThreadPoolExecutor newThreadPool(String threadPrefix, int corePoolSize,
                                                   int maximumPoolSize, int queueCapacity) {
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(corePoolSize)
                .maximumPoolSize(maximumPoolSize)
                .workQueue(QueueTypeEnum.MEMORY_SAFE_LINKED_BLOCKING_QUEUE.getName(), queueCapacity, null)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }
```

### 责任链模式简单封装

common 模块 pattern.filter 包下提供简单责任链模式的封装，在通知告警模块有应用，可以参考

### 内存安全阻塞队列

common 模块 queue 包提供内存安全阻塞队列 MemorySafeLinkedBlockingQueue，开发中有需求可以选择使用

### 配置文件解析器

core 模块提供 json、properties、yaml 类型配置文件解析器，有需要可以使用