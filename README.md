###  轻量级动态线程池 - DynamicTp

***

[![7SaOzV.md.png](https://s4.ax1x.com/2022/01/06/7SaOzV.md.png)](https://imgtu.com/i/7SaOzV)



#### 简介

- 基于Spring框架，现只支持Spring项目使用
- 基于配置中心实现线程池参数动态调整，实时生效；集成主流配置中心，默认支持Nacos、Apollo，同时提供SPI接口可自定义扩展实现

+ 内置通知报警功能，提供多种报警维度（配置变更通知、活性报警、容量阈值报警、拒绝触发报警），默认支持企业微信、钉钉报警，同时提供SPI接口可自定义扩展实现
+ 内置简单线程池指标采集功能，支持通过MicroMeter、日志输出、Endpoint三种方式，可自定义扩展
+ 轻量级，引入spring-boot-starter，即可食用

