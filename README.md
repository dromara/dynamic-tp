<p align="center">
	<img alt="logo" src="resources/img/logo.png" width="50%">
</p>
<p align="center">
	<strong>A lightweight dynamic thread pool based on configuration centers, with built-in monitoring & alerting, middleware thread pool management, and SPI extensibility</strong>
</p>

<p align="center">
  <a href="https://gitee.com/dromara/dynamic-tp"><img src="https://gitee.com/dromara/dynamic-tp/badge/star.svg"></a>
  <a href="https://gitee.com/dromara/dynamic-tp/members"><img src="https://gitee.com/dromara/dynamic-tp/badge/fork.svg"></a>
  <a href="https://github.com/dromara/dynamic-tp"><img src="https://img.shields.io/github/stars/dromara/dynamic-tp?style=flat-square&logo=github"></a>
  <a href="https://github.com/dromara/dynamic-tp/network/members"><img src="https://img.shields.io/github/forks/dromara/dynamic-tp?style=flat-square&logo=GitHub"></a>
  <a href='https://gitcode.com/dromara/dynamic-tp'><img src='https://gitcode.com/dromara/dynamic-tp/star/badge.svg' alt='star'></a>
  <a href="https://github.com/dromara/dynamic-tp/blob/master/LICENSE"><img src="https://img.shields.io/github/license/dromara/dynamic-tp.svg?style=flat-square"></a>
</p>

<p align="center">
    🌐 <a href="README_CN.md">中文</a> | English
</p>

<p align="center">
    Website: <a href="https://dynamictp.cn">https://dynamictp.cn</a> 🔥
</p>

---

## Pain Points

Have you encountered these issues when using `ThreadPoolExecutor`?

> 1. You created a `ThreadPoolExecutor`, but have no idea what values to set for the core parameters.
>
> 2. You set parameters based on experience, only to find after deployment that they need adjustment — requiring code changes and redeployment.
>
> 3. Thread pools are a black box to developers. You can't monitor their behavior until something breaks.

If so, **DynamicTp** may be the solution.

`ThreadPoolExecutor` provides set/get methods and extension points for its core parameters, enabling runtime dynamic modification.

<details>
<summary>👉 ThreadPoolExecutor dynamic methods (click to expand)</summary>

```java
// --- Set ---
public void setCorePoolSize(int corePoolSize);                    // core pool size
public void setMaximumPoolSize(int maximumPoolSize);              // max pool size
public void setKeepAliveTime(long time, TimeUnit unit);           // thread idle keep-alive time
public void setThreadFactory(ThreadFactory threadFactory);        // thread factory
public void setRejectedExecutionHandler(RejectedExecutionHandler handler); // rejection policy
public void allowCoreThreadTimeOut(boolean value);                // allow core threads to time out

// --- Get ---
public int getCorePoolSize();
public int getMaximumPoolSize();
public long getKeepAliveTime(TimeUnit unit);
public BlockingQueue<Runnable> getQueue();                        // task queue
public RejectedExecutionHandler getRejectedExecutionHandler();
public boolean allowsCoreThreadTimeOut();

// --- Extension hooks ---
protected void beforeExecute(Thread t, Runnable r);               // before task execution
protected void afterExecute(Runnable r, Throwable t);             // after task execution
```

</details>

Most modern internet projects adopt microservice architecture with a service governance stack. The distributed configuration center plays a key role — enabling real-time configuration changes with instant effect.

So, can we combine a configuration center to dynamically adjust thread pool parameters at runtime?

Absolutely. Configuration centers are highly available, relieving concerns about config push failures and reducing the effort of building a dynamic thread pool solution from scratch.

**Background summary:**

- **Ubiquity**: Thread pools are a fundamental tool used by 90%+ of Java developers to improve system performance.

- **Uncertainty**: Projects often contain many thread pools — some IO-intensive, some CPU-intensive — and optimal core parameters are hard to determine upfront, requiring runtime tuning.

- **Lack of visibility**: Thread pool metrics are invisible during operation. A monitoring & alerting mechanism is needed to detect issues before and during incidents.

- **High availability**: Configuration changes must be reliably pushed to clients. Leveraging an existing configuration center greatly improves system availability.

---

## Features

Based on the above analysis, we extended `ThreadPoolExecutor` with the following goals:

> 1. Dynamic parameter modification at runtime, taking effect instantly.
>
> 2. Real-time monitoring of thread pool status with alerting, pushing notifications to office platforms.
>
> 3. Periodic metric collection, integrated with visualization platforms like Grafana for dashboards.
>
> 4. Thread pool management for commonly used third-party middleware.

**Latest version features:** ✅

- **Zero code intrusion**: All configuration lives in the configuration center. At startup, thread pools are created from config and registered in the Spring container — inject and use directly, zero impact on business code.

- **Lightweight & simple**: Get started in 3 minutes with just 4 steps. Simply add the dependency, configure, annotate, and inject.

- **Notifications & alerting**: Multiple alert dimensions (config change, thread activity, queue capacity, rejection, task execution/wait timeout). Supports WeCom, DingTalk, Feishu, Email, and extensible via SPI.

- **Monitoring**: 20+ metrics (pool, queue, task, TPS, TPxx) collected via MicroMeter, JsonLog, JMX, or Spring Boot Endpoint. Extensible via SPI.

- **Task enhancement**: Powerful task wrapping (stronger than Spring's built-in). Implement `TaskWrapper` interface for MDC, TTL, trace context propagation (e.g., MdcTaskWrapper, TtlTaskWrapper, OpenTelemetryWrapper).

- **Multi configuration center support**: Nacos, Apollo, Zookeeper, Consul, Etcd, Polaris, ServiceComb. Extensible via SPI.

- **Middleware thread pool management**: Integrated with Tomcat, Jetty, Undertow, Dubbo, RocketMQ, Hystrix, gRPC, Motan, OkHttp3, Brpc, Tars, SofaRPC, RabbitMQ, Liteflow, Thrift (dynamic tuning, monitoring, alerting).

- **Multiple pool modes**: `DtpExecutor` (enhanced), `EagerDtpExecutor` (IO-intensive), `ScheduledDtpExecutor` (scheduled), `OrderedDtpExecutor` (ordered). Choose based on your scenario.

- **Compatibility**: Standard JUC thread pools and Spring `ThreadPoolTaskExecutor` can be managed by adding `@DynamicTp` on the `@Bean` definition.

- **Graceful shutdown**: Leverages Spring lifecycle management to shut down thread pools gracefully, processing as many queued tasks as possible before shutdown.

- **Highly extensible**: Core features expose SPI interfaces for custom implementations (config center, config parsing, alerting, metrics collection, task wrapping, rejection policies, etc.).

- **Battle-tested at scale**: Inspired by [Meituan's thread pool practice](https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html), with mature production experience at Meituan.

---

## Architecture

**The framework is divided into the following modules:**

> 1. Configuration change listener
>
> 2. Thread pool management
>
> 3. Monitoring
>
> 4. Notification & alerting
>
> 5. Third-party middleware thread pool management

![Architecture](resources/img/arch.svg)

See the official documentation for details: [Architecture](https://dynamictp.cn/guide/introduction/architecture.html)

---

## Quick Start

> 1. Add the dependency for your configuration center (see official docs).
>
> 2. Configure thread pool instances in your configuration center (see official docs).
>
> 3. Add `@EnableDynamicTp` annotation to your application class.
>
> 4. Inject via `@Resource` / `@Autowired`, or retrieve with `DtpRegistry.getExecutor("name")`.

For detailed examples, see the `example` module and the [official documentation](https://dynamictp.cn/guide/use/quick-start.html).

---

## Alerting

- Alert notifications are pushed when thresholds are triggered (thread activity, queue capacity, rejection, task wait/execution timeout), with highlighted fields.

<img src="resources/img/alarm.jpg" alt="Alert" width="50%" />

- Configuration change notifications are pushed with highlighted changed fields.

<img src="resources/img/notice.jpg" alt="Config Change Notification" width="50%" />

---

## Monitoring

![Monitoring Data 1](resources/img/monitor1.jpg)
![Monitoring Data 2](resources/img/monitor2.jpg)
![Monitoring Data 3](resources/img/monitor3.jpg)

Four metric collection modes are available, configured via `collectorTypes` (default: Micrometer):

> 1. **Logging**: Metrics output as JSON to a dedicated log file.
>
> 2. **Internal_logging**: Metrics output as JSON to the application log.
>
> 3. **Micrometer**: Uses the Micrometer facade to collect metrics into storage platforms (Prometheus, InfluxDB, etc.).
>
> 4. **Endpoint**: Exposes a Spring Boot Endpoint for real-time metric retrieval via HTTP.

> 📖 See the official documentation for details: [Alerting](https://dynamictp.cn/guide/notice/alarm.html) | [Monitoring](https://dynamictp.cn/guide/monitor/collect_types.html)

---

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=dromara/dynamic-tp&type=Date)](https://star-history.com/#dromara/dynamic-tp&Date)

---

## Repository

- GitHub: https://github.com/dromara/dynamic-tp
- Gitee: https://gitee.com/dromara/dynamic-tp
- GitCode: https://gitcode.com/dromara/dynamic-tp

---

## Contact

If you find this project helpful, **please give us a star** — your support drives us forward!

For questions, ideas, or suggestions, join our community to chat with 1700+ members.

Follow the WeChat public account and add my personal WeChat (note: dynamic-tp) to join the group.

![](resources/img/contact.jpg)

To help the project grow, please register here: [User Registration](https://dynamictp.cn/guide/other/users.html)

---

## Sponsors

- Easysearch: Enterprise-grade distributed search database.

<a href="https://easysearch.cn/" target="_blank">
    <img class="no-zoom" src="resources/img/easysearch.png" width="50%" height="50%"/>
</a>

---

## Related Projects

- [HertzBeat](https://github.com/dromara/hertzbeat): An easy-to-use, real-time monitoring and alerting system with powerful custom monitoring capabilities, no Agent required.
