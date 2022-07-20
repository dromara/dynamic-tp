---
title: micrometer接入流程
icon: config
order: 1
author: yanhom
date: 2022-06-11
category:
  - micrometer接入流程
tag:
  - micrometer接入流程
sticky: true
star: true
---

## 集成步骤

这块要讲的是集成prometheus+grafana做监控，事先你得安装好prometheus+grafana，这个就不展开讲了，网上教程很多，测试使用可以直接用docker安装，非常简单，安装完之后接着往下看。

1.首先配置文件中开启micrometer数据采集

```yaml
   enabledCollect: true
   collectorType: micrometer
```

2.项目中引入 micrometer-prometheus 依赖

```xml
  <dependency>
      <groupId>io.micrometer</groupId>
      <artifactId>micrometer-registry-prometheus</artifactId>
  </dependency>
```

3.开启prometheus指标采集端点

```yaml
management:
  metrics:
    export:
      prometheus: 
        enabled: true
  endpoints:
    web:
      exposure:
        include: '*'   # 测试使用，线上不要用*，按需开启
```

4.配置prometheus数据采集job，这块可以去了解下他的SD机制（Service Discovery），也就是自动到注册中心发现服务，看你所用的注册中心支不支持这种方式，[可以去官网查看](https://prometheus.io/docs/prometheus/latest/configuration/configuration/#scrape_config)，k8s，ZK，Eureka、Consul等都是支持的。这里使用static_configs方式，简单的指定地址的静态配置

```yaml
- job_name: 'prometheus'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['192.168.2.104:9098']
```

job配置后prometheus管理台看到如下图所示，说明已经开始正常采集指标配置

![采集指标](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/435f0a69790946f8bff7761c40a0a0db~tplv-k3u1fbpfcp-zoom-1.image)

5.然后就是配置grafana数据可视化，配置如下图，需要该pannel配置Json的可以加我发你，到这里监控就搭建起来了，其实也很简单，然后就可以实时监控线程池数据指标变动了

![监控数据](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a36430c06bf44ca987ff54b500a14172~tplv-k3u1fbpfcp-zoom-1.image)


::: warning

完成上述所有步骤后，记得重新修改下每个pannel的数据源，这样才会正确显示监控数据

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/39e2c37af1fb48679b5fdd56e7f89c37~tplv-k3u1fbpfcp-watermark.image?)

:::