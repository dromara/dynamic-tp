# Dynamic-TP 代码质量优化清单

> 生成日期: 2026-06-24
> 覆盖范围: core, common, spring, logging, adapter, starter, extension, jvmti 的 production 代码
> 排除: example/、benchmark/、test/ 目录；starter/adapter 下纯 AutoConfiguration 类（无业务逻辑）
> 说明: "缺少直接单测"指无同名/直接对应测试文件，聚合测试可能间接覆盖部分逻辑
> 每个优化点均为原子粒度：一行日志 = 一个点，一个单测场景 = 一个点

---

## 一、日志 BUG / 空 catch 块 / 异常吞没（P0 严重）

| # | 文件 | 问题描述 |
|---|------|----------|
| 1 | `adapter/adapter-dubbo/.../alibaba/AlibabaDubboDtpAdapter.java:83` | `catch (Throwable e) { }` 空 catch，轮询初始化循环中吞没所有异常，应加 `log.debug("...init failed, retrying...", e)` |
| 2 | `starter/starter-adapter/.../undertow/UndertowDtpAdapter.java:69` | **BUG** `log.warn("...{} enhance failed, taskPool is null.")` 有 `{}` 占位符但未传参，输出字面 `{}`。修复：传入 `getTpName()` 或移除占位符 |
| 3 | `common/.../util/MethodUtil.java:42` | `invokeAndReturnDouble` catch Exception 后直接 return -1，无日志 |
| 4 | `common/.../util/MethodUtil.java:57` | `invokeAndReturnLong` 同上，catch Exception 后静默返回 -1 |
| 5 | `common/.../util/MethodUtil.java:72` | `invokeAndReturnInt` 同上，catch Exception 后静默返回 -1 |
| 6 | `common/.../util/DtpPropertiesBinderUtil.java:174` | `catch (Exception e) { // ignore }` 吞没 pluginNames 反射异常 |
| 7 | `spring/.../DtpPostProcessor.java` | `catch (IllegalAccessException ignored) { }` 吞没反射异常 |
| 8 | `core/.../system/MemoryMetricsCaptor.java:82` | `catch (InternalError) { return null; }` 无日志，getUsage() 失败完全不可见 |
| 9 | `core/.../system/OperatingSystemBeanManager.java:112` | `catch (Exception) { return null; }` deduceMethod() 中无日志 |
| 10 | `jvmti/jvmti-runtime/.../NativeUtil.java` | 使用 `assert is != null` 检查资源，运行时 assert 默认关闭，应改为显式 null 检查 + 日志 |
| 11 | `starter/starter-configcenter/starter-etcd/.../EtcdListener.java` | `@SneakyThrows` on onNext()，`response.getEvents().get(0)` 无空列表检查，IndexOutOfBoundsException 无上下文 |
| 12 | `starter/starter-configcenter/starter-etcd/.../EtcdConfigEnvironmentProcessor.java` | `@SneakyThrows` on postProcessEnvironment()，Etcd 连接失败时整个 Spring 上下文启动失败，无 try/catch 无日志 |
| 13 | `extension/extension-notify-email/.../EmailNotifier.java` | `@SneakyThrows` on send0()，邮件发送失败（SMTP 宕机、地址错误）无日志直接抛异常 |
| 14 | `starter/starter-configcenter/starter-apollo/.../ApolloRefresher.java:141` | isDtpNamespace() catch IOException 静默返回 false，配置文件格式错误不可见 |
| 15 | `core/.../monitor/collector/jmx/JMXCollector.java:62` | **BUG** `GAUGE_CACHE.put()` 在 catch 块之后执行，MBean 注册失败仍会缓存 stats 对象，后续调用掩盖错误 |

## 二、日志问题 — catch 中丢失异常对象（P1）

| # | 文件 | 问题描述 |
|---|------|----------|
| 16 | `logging/.../DtpLoggingInitializer.java` | catch 块 `log.error("...failed...")` 未传入 ClassNotFoundException 异常对象 |
| 17 | `logging/.../logback/DtpLogbackLogging.java` | `log.error("Cannot initialize dtp logback logging.")` 未传入异常对象 |
| 18 | `logging/.../log4j2/DtpLog4j2Logging.java` | `log.error("Cannot initialize dtp log4j2 logging.")` 未传入异常对象 |
| 19 | `common/.../util/VersionUtil.java:36` | `log.warn("no version number found")` 未传入异常 e |
| 20 | `common/.../parser/json/AbstractJsonParser.java:36` | `log.warn` 关于缺少类时未传入 ClassNotFoundException |

## 三、日志问题 — 消息描述不够好 / 缺少上下文（P2）

| # | 文件 | 问题描述 |
|---|------|----------|
| 21 | `common/.../util/DingSignUtil.java` | `log.error("cal ding sign error")` 过于笼统，应包含 timestamp 等上下文 |
| 22 | `core/.../system/CpuMetricsCaptor.java:62` | `log.error("Get system metrics error.", e)` 缺少具体指标名；catching Throwable 过宽 |
| 23 | `core/.../monitor/DtpMonitor.java:88` | `log.error("DynamicTp monitor, run error", e)` 缺少 executor 名称 |
| 24 | `common/.../util/JsonUtil.java:53` | `log.error("Failed to load JSON parser", e)` 缺少 SPI class 名称 |
| 25 | `common/.../util/JsonUtil.java` | 无 parser 可用时抛 IllegalStateException 前未 log.error |
| 26 | `core/.../notifier/manager/AlarmManager.java:152` | `log.error("Unsupported alarm type")` 用 error 级别但这是逻辑分支，应改为 warn |
| 27 | `core/.../monitor/collector/jmx/JMXCollector.java:60` | `log.error("collect thread pool stats error", e)` 缺少 poolName 上下文 |
| 28 | `starter/starter-configcenter/starter-etcd/.../EtcdUtil.java` | getConfigMap catch 块日志缺少 key/endpoint 上下文 |

## 四、日志问题 — 字符串拼接（P2 低）

| # | 文件 | 问题描述 |
|---|------|----------|
| 29 | `common/.../timer/HashedWheelTimer.java:424` | log.error 使用字符串拼接（vendored Dubbo 代码，改收益有限但可顺手修） |
| 30 | `common/.../timer/HashedWheelTimer.java:656` | log.warn 使用字符串拼接（同上，vendored 代码） |
| 31 | `starter/starter-configcenter/starter-etcd/.../EtcdListener.java:51` | `log.info("..." + key)` 字符串拼接，应改为 `log.info("..., key:{}", key)` |
| 32 | `starter/starter-configcenter/starter-etcd/.../EtcdListener.java:54` | 同上，第 2 处 |
| 33 | `starter/starter-configcenter/starter-etcd/.../EtcdListener.java:60` | 同上，第 3 处 |
| 34 | `starter/starter-configcenter/starter-etcd/.../EtcdListener.java:71` | 同上，第 4 处 |

## 五、日志问题 — HTTP 通知缺少失败路径日志（P2）

| # | 文件 | 问题描述 |
|---|------|----------|
| 35 | `common/.../notifier/AbstractHttpNotifier.java:51` | 只在 response 非 null 时 log.info 成功，缺少 HTTP 非 2xx 状态码的 warn 日志 |
| 36 | `common/.../notifier/AbstractHttpNotifier.java` | response 为 null 时无任何日志（网络超时等场景） |

## 六、日志问题 — 异常传播导致循环中断（P2）

| # | 文件 | 问题描述 |
|---|------|----------|
| 37 | `core/.../notifier/AbstractDtpNotifier.java:77,87` | `notifier.send()` 无 try-catch，一个平台发送异常会中断剩余平台的发送 |
| 38 | `core/.../handler/NotifierHandler.java:66-69` | sendNotice 中一个平台异常会中断剩余平台（同上） |
| 39 | `core/.../handler/NotifierHandler.java:78-81` | sendAlarm 中同上 |
| 40 | `core/.../handler/CollectorHandler.java:63-68` | 一个 collector.collect() 异常会中断剩余 collector |
| 41 | `core/.../notifier/chain/invoker/AlarmInvoker.java` | NotifierHandler.sendAlarm() 异常未捕获，传播到 alarm chain |
| 42 | `core/.../notifier/chain/invoker/NoticeInvoker.java` | NotifierHandler.sendNotice() 异常未捕获，传播到 notice chain |

## 七、日志问题 — 静默丢弃任务/事件（P2）

| # | 文件 | 问题描述 |
|---|------|----------|
| 43 | `core/.../notifier/manager/AlarmManager.java` | ALARM_EXECUTOR 使用 DiscardOldestPolicy，队列满时任务静默丢弃无日志 |
| 44 | `core/.../notifier/manager/NoticeManager.java` | NOTICE_EXECUTOR 同上，DiscardOldestPolicy 静默丢弃 |
| 45 | `common/.../manager/EventBusManager.java:58` | `post()` 无 try-catch，Guava EventBus 默认吞没 subscriber 异常 |

## 八、日志问题 — Adapter / Starter 静默失败（P3）

| # | 文件 | 问题描述 |
|---|------|----------|
| 46 | `adapter/adapter-dubbo/.../apache/ApacheDubboDtpAdapter.java:116` | 反射 `data` 字段为 null 时静默返回，无 log.warn |
| 47 | `adapter/adapter-dubbo/.../apache/ApacheDubboDtpAdapter.java:141` | handlers 为空时静默返回，无 debug 日志 |
| 48 | `adapter/adapter-liteflow/.../LiteflowDtpAdapter.java:78` | 反射获取 executorService 可能为 null，无 null 检查无日志 |
| 49 | `adapter/adapter-okhttp3/.../Okhttp3DtpAdapter.java:74` | 反射字段均失败时无 warn 日志 |
| 50 | `adapter/adapter-brpc/.../server/StarlightServerDtpAdapter.java:64` | bean 类型不匹配时静默返回 |
| 51 | `adapter/adapter-brpc/.../server/StarlightServerDtpAdapter.java:72` | uri 为 null 时静默返回 |
| 52 | `adapter/adapter-brpc/.../server/StarlightServerDtpAdapter.java:76` | processor 为 null 时静默返回 |
| 53 | `adapter/adapter-brpc/.../server/StarlightServerDtpAdapter.java:80` | threadPoolFactory 为 null 时静默返回 |
| 54 | `adapter/adapter-brpc/.../client/StarlightClientDtpAdapter.java:72` | threadPoolFactory 为 null 时静默 |
| 55 | `starter/starter-common/.../DtpEndpoint.java` | invoke() 无 try-catch，一个 executor 异常导致整个 actuator 端点失败 |
| 56 | `starter/starter-adapter/.../tomcat/TomcatExecutorProxy.java:67` | catch(Throwable) 反射设置 reject handler 失败时静默降级，无日志 |
| 57 | `starter/starter-configcenter/starter-etcd/.../EtcdUtil.java` | 静态 `resultMap = Maps.newHashMap()` 非线程安全，并发 watch 事件会损坏 |

## 九、日志问题 — JVMTI 模块（P3）

| # | 文件 | 问题描述 |
|---|------|----------|
| 58 | `jvmti/jvmti-runtime/.../JVMTI.java` | `getInstances` 不可用时静默返回空列表 |
| 59 | `jvmti/jvmti-runtime/.../NativeUtil.java` | 整个类无任何日志（加载成功/失败均不可见） |
| 60 | `jvmti/jvmti-runtime/.../JVMTIUtil.java` | 未知 OS 时 libName 为 null，无 warn |
| 61 | `jvmti/jvmti-runtime/.../OSUtils.java` | 未知 arch 时静默返回原始字符串 |

## 十、日志问题 — 其他零散项（P3）

| # | 文件 | 问题描述 |
|---|------|----------|
| 62 | `common/.../manager/ContextManagerHelper.java:38-40` | 设置 NullContextManager 后立即 throw，fallback 赋值是死代码 |
| 63 | `core/.../handler/CollectorHandler.java:64` | 未知 collectorType 静默跳过，无 warn |
| 64 | `core/.../handler/NotifierHandler.java:66` | sendNotice 未知平台时静默跳过 |
| 65 | `core/.../handler/NotifierHandler.java:78` | sendAlarm 未知平台时静默跳过 |
| 66 | `common/.../util/ExtensionServiceLoader.java` | SPI 迭代可能抛 ServiceConfigurationError，未捕获 |
| 67 | `common/.../util/ExtensionServiceLoader.java` | SPI 结果为空时无 debug 日志 |
| 68 | `common/.../plugin/DtpInterceptorRegistry.java` | 无 @DtpIntercepts 注解的拦截器静默忽略 |
| 69 | `core/.../DtpRegistry.java:117` | `putIfAbsent` 返回非 null（名称已存在）时静默，无 warn |
| 70 | `core/.../support/task/wrapper/TaskWrappers.java` | getByNames() 未识别的 wrapper 名称静默忽略 |
| 71 | `core/.../reject/RejectedInvocationHandler.java` | catch InvocationTargetException 后 unwrap + rethrow，无日志 |
| 72 | `core/.../executor/OrderedDtpExecutor.java` | 两处 catch 块 rethrow 无日志，有序任务执行失败不可诊断 |
| 73 | `core/.../executor/eager/EagerDtpExecutor.java:108` | catch InterruptedException 包装为 RejectedExecutionException，丢失原始中断上下文 |
| 74 | `core/.../lifecycle/DtpLifecycle.java` | start()/stop() 无日志，生命周期转换不可见；shutdownInternal 5 个 destroy 无 error handling |
| 75 | `core/.../support/selector/HashedExecutorSelector.java` | **BUG** `arg.hashCode() % size` 可能为负（Integer.MIN_VALUE % n），应用 `(hashCode & 0x7fffffff) % size` |

---

## 十一、缺少直接单测 — Core 模块

| # | 源文件 | 说明 |
|---|--------|------|
| 76 | `core/.../notifier/DtpLarkNotifier.java` | 已补充直接单测 |
| 77 | `core/.../notifier/DtpDingNotifier.java` | 缺少直接单测 |
| 78 | `core/.../notifier/manager/NotifyHelper.java` | getNotifyItem / getPlatform 逻辑未覆盖 |
| 79 | `core/.../notifier/manager/NoticeManager.java` | 缺少直接单测 |
| 80 | `core/.../notifier/chain/invoker/AlarmInvoker.java` | 缺少直接单测 |
| 81 | `core/.../notifier/chain/invoker/NoticeInvoker.java` | 缺少直接单测 |
| 82 | `core/.../notifier/chain/filter/BaseAlarmFilter.java` | 缺少直接单测 |
| 83 | `core/.../notifier/chain/filter/BaseNoticeFilter.java` | 缺少直接单测 |
| 84 | `core/.../notifier/alarm/AlarmLimiter.java` | AlarmCounterTest 存在但 AlarmLimiter 无 |
| 85 | `core/.../notifier/context/AlarmCtx.java` | 缺少直接单测 |
| 86 | `core/.../notifier/context/NoticeCtx.java` | 缺少直接单测 |
| 87 | `core/.../notifier/context/DtpNotifyCtxHolder.java` | 缺少直接单测 |
| 88 | `core/.../timer/QueueTimeoutTimerTask.java` | 缺少直接单测 |
| 89 | `core/.../timer/RunTimeoutTimerTask.java` | 缺少直接单测 |
| 90 | `core/.../system/CpuMetricsCaptor.java` | 缺少直接单测 |
| 91 | `core/.../system/MemoryMetricsCaptor.java` | 缺少直接单测 |
| 92 | `core/.../system/SystemMetricManager.java` | 缺少直接单测 |
| 93 | `core/.../monitor/collector/jmx/JMXCollector.java` | 缺少直接单测（GAUGE_CACHE bug 无回归保护） |
| 94 | `core/.../converter/ExecutorConverter.java` | toMetrics 边界场景可补充 |
| 95 | `core/.../support/selector/HashedExecutorSelector.java` | 缺少直接单测（负数 modulo bug 无回归保护） |

## 十二、缺少直接单测 — Common 模块

| # | 源文件 | 说明 |
|---|--------|------|
| 96 | `common/.../manager/ContextManagerHelper.java` | 静态初始化 + NullContextManager fallback 未覆盖 |
| 97 | `common/.../plugin/DtpInterceptorRegistry.java` | 注册逻辑和无注解拦截器忽略行为未覆盖 |
| 98 | `common/.../parser/json/JacksonParser.java` | JsonParserTest 存在但可能未覆盖 Jackson 特有路径 |
| 99 | `common/.../parser/json/FastJsonParser.java` | 缺少直接单测 |
| 100 | `common/.../parser/json/GsonParser.java` | 缺少直接单测 |
| 101 | `common/.../util/MethodUtil.java` | 3 个 invoke 方法的 catch 路径未覆盖 |

## 十三、缺少直接单测 — Spring / Logging 模块

| # | 源文件 | 说明 |
|---|--------|------|
| 102 | `logging/.../log4j2/DtpLog4j2Logging.java` | LoggingTest 仅覆盖 logback |
| 103 | `logging/.../DtpLoggingInitializer.java` | 检测/错误分支未覆盖 |
| 104 | `logging/.../logback/DtpLogbackLogging.java` | 错误分支未覆盖 |
| 105 | `spring/.../AbstractSpringRefresher.java` | 缺少直接单测 |

## 十四、缺少直接单测 — JVMTI 模块

| # | 源文件 | 说明 |
|---|--------|------|
| 106 | `jvmti/jvmti-runtime/.../OSUtils.java` | normalizeArch/normalize 可参数化测试 |
| 107 | `jvmti/jvmti-runtime/.../JVMTIUtil.java` | OS 检测逻辑未覆盖 |
| 108 | `jvmti/jvmti-runtime/.../NativeUtil.java` | 资源提取逻辑未覆盖 |
| 109 | `jvmti/jvmti-runtime/.../JVMTI.java` | getInstance/getInstances/forceGc 未覆盖 |

## 十五、缺少直接单测 — Adapter 模块

| # | 源文件 | 说明 |
|---|--------|------|
| 110 | `adapter/adapter-dubbo/.../alibaba/AlibabaDubboDtpAdapter.java` | 缺少直接单测 |
| 111 | `adapter/adapter-dubbo/.../apache/ApacheDubboDtpAdapter.java` | 测试文件存在但未实际导入适配器类 |
| 112 | `adapter/adapter-liteflow/.../LiteflowDtpAdapter.java` | 缺少直接单测 |
| 113 | `adapter/adapter-okhttp3/.../Okhttp3DtpAdapter.java` | 缺少直接单测 |
| 114 | `adapter/adapter-brpc/.../server/StarlightServerDtpAdapter.java` | 缺少直接单测 |
| 115 | `adapter/adapter-brpc/.../client/StarlightClientDtpAdapter.java` | 缺少直接单测 |

## 十六、缺少直接单测 — Extension 模块

| # | 源文件 | 说明 |
|---|--------|------|
| 116 | `extension/extension-notify-email/.../EmailNotifier.java` | 缺少直接单测 |
| 117 | `extension/extension-notify-email/.../DtpEmailNotifier.java` | 缺少直接单测 |
| 118 | `extension/extension-notify-yunzhijia/.../YunZhiJiaNotifier.java` | 缺少直接单测 |
| 119 | `extension/extension-notify-yunzhijia/.../DtpYunZhiJiaNotifier.java` | 缺少直接单测 |
| 120 | `extension/extension-opentelemetry/.../OpenTelemetryWrapper.java` | 缺少直接单测 |
| 121 | `extension/extension-skywalking/.../SwTraceTaskWrapper.java` | 缺少直接单测 |

## 十七、缺少直接单测 — Starter 模块

| # | 源文件 | 说明 |
|---|--------|------|
| 122 | `starter/starter-common/.../DtpEndpoint.java` | 缺少直接单测 |
| 123 | `starter/starter-common/.../SpringBootPropertiesBinder.java` | 缺少直接单测 |
| 124 | `starter/starter-configcenter/starter-etcd/.../EtcdUtil.java` | 缺少直接单测（线程安全 bug 无回归保护） |
| 125 | `starter/starter-configcenter/starter-zookeeper/.../CuratorUtil.java` | 缺少直接单测 |
| 126 | `starter/starter-adapter/.../undertow/UndertowDtpAdapter.java` | 缺少直接单测 |
| 127 | `starter/starter-adapter/.../tomcat/TomcatDtpAdapter.java` | 缺少直接单测 |
| 128 | `starter/starter-adapter/.../jetty/JettyDtpAdapter.java` | 缺少直接单测 |

## 十八、其他代码质量改进

| # | 文件 | 问题描述 |
|---|------|----------|
| 129 | `core/.../notifier/DtpDingNotifier.java` | `@Slf4j` 声明但未使用 |
| 130 | `core/.../notifier/DtpLarkNotifier.java` | `@Slf4j` 声明但未使用 |
| 131 | `core/.../notifier/DtpWechatNotifier.java` | `@Slf4j` 声明但未使用 |
| 132 | `core/.../executor/DtpExecutor.java` | `@Slf4j` 声明但未使用 |
| 133 | `core/.../executor/NamedThreadFactory.java` | `@Slf4j` 声明但未使用 |
| 134 | `core/.../executor/OrderedDtpExecutor.java` | `@Slf4j` 声明但未使用 |
| 135 | `core/.../executor/priority/PriorityDtpExecutor.java` | `@Slf4j` 声明但未使用 |
| 136 | `core/.../aware/TaskTimeoutAware.java` | `@Slf4j` 声明但未使用 |
| 137 | `core/.../support/task/runnable/EnhancedRunnable.java` | `@Slf4j` 声明但未使用 |
| 138 | `core/.../handler/CollectorHandler.java` | `@Slf4j` 声明但未使用（本身就有日志缺失问题） |
| 139 | `core/.../handler/NotifierHandler.java` | `@Slf4j` 声明但未使用 |
| 140 | `core/.../lifecycle/DtpLifecycle.java` | `@Slf4j` 声明但未使用 |
| 141 | `core/.../system/SystemMetricManager.java` | `@Slf4j` 声明但未使用 |
| 142 | `common/.../manager/ContextManagerHelper.java` | `@Slf4j` 声明但未使用 |
| 143 | `common/.../em/RejectedTypeEnum.java` | `@Slf4j` 声明但未使用 |
| 144 | `jvmti/jvmti-runtime/.../OSUtils.java` | platform/arch 字段非 volatile 非 final，理论竞态 |
| 145 | `jvmti/jvmti-runtime/.../NativeUtil.java` | 并发 loadLibraryFromJar 时临时目录竞态 |
| 146 | `jvmti/jvmti-runtime/.../JVMTI.java` | forceGc 未检查 AVAILABLE，native lib 加载失败时抛 UnsatisfiedLinkError |

---

## 任务列表（按优先级排序）

### P0 — 空 catch / 异常吞没 / BUG（必须修复）

- [ ] 1. `AlibabaDubboDtpAdapter:83` — 空 catch(Throwable) 加 debug 日志
- [ ] 2. `UndertowDtpAdapter:69` — log.warn `{}` 占位符补参数或移除
- [ ] 3. `MethodUtil:42` — invokeAndReturnDouble catch 加 warn 日志
- [ ] 4. `MethodUtil:57` — invokeAndReturnLong catch 加 warn 日志
- [ ] 5. `MethodUtil:72` — invokeAndReturnInt catch 加 warn 日志
- [ ] 6. `DtpPropertiesBinderUtil:174` — catch 加 debug 日志
- [ ] 7. `DtpPostProcessor` — catch(IllegalAccessException) 加 debug 日志
- [ ] 8. `MemoryMetricsCaptor:82` — catch(InternalError) 加 debug 日志
- [ ] 9. `OperatingSystemBeanManager:112` — catch(Exception) 加 debug 日志
- [ ] 10. `NativeUtil` — assert 改为显式 null 检查 + 日志
- [ ] 11. `EtcdListener` — @SneakyThrows 改为 try-catch + 日志，events 空列表检查
- [ ] 12. `EtcdConfigEnvironmentProcessor` — @SneakyThrows 改为 try-catch + log.error
- [ ] 13. `EmailNotifier.send0()` — @SneakyThrows 改为 try-catch + log.error
- [ ] 14. `ApolloRefresher:141` — catch IOException 加 log.debug
- [ ] 15. `JMXCollector:62` — GAUGE_CACHE.put 移入 try 块内（注册成功才缓存）
- [ ] 16. `HashedExecutorSelector` — 负数 modulo 修复：`(hashCode & 0x7fffffff) % size`

### P1 — catch 中丢失异常对象

- [ ] 17. `DtpLoggingInitializer` — log.error 加入异常对象
- [ ] 18. `DtpLogbackLogging` — log.error 加入异常对象
- [ ] 19. `DtpLog4j2Logging` — log.error 加入异常对象
- [ ] 20. `VersionUtil:36` — log.warn 加入异常 e
- [ ] 21. `AbstractJsonParser:36` — log.warn 加入 ClassNotFoundException

### P2 — 日志消息改进 / 补充上下文

- [ ] 22. `DingSignUtil` — log.error 消息加入 timestamp 上下文
- [ ] 23. `CpuMetricsCaptor:62` — log.error 消息加入指标名；catch Throwable 改为 Exception
- [ ] 24. `DtpMonitor:88` — log.error 消息加入 executor 名称
- [ ] 25. `JsonUtil:53` — log.error 消息加入 SPI class 名称
- [ ] 26. `JsonUtil` — 无 parser 时抛异常前加 log.error
- [ ] 27. `AlarmManager:152` — error 级别改为 warn
- [ ] 28. `JMXCollector:60` — log.error 加入 poolName
- [ ] 29. `EtcdUtil.getConfigMap` — catch 日志加入 key/endpoint 上下文

### P2 — 字符串拼接 → 参数化日志

- [ ] 30. `HashedWheelTimer:424` — 字符串拼接改参数化（vendored 代码，收益有限）
- [ ] 31. `HashedWheelTimer:656` — 同上
- [ ] 32. `EtcdListener:51` — `log.info("..." + key)` → `log.info("..., key:{}", key)`
- [ ] 33. `EtcdListener:54` — 同上
- [ ] 34. `EtcdListener:60` — 同上
- [ ] 35. `EtcdListener:71` — 同上

### P2 — HTTP 通知 / 异常传播 / 静默丢弃

- [ ] 36. `AbstractHttpNotifier:51` — 加 HTTP 非 2xx 状态码 warn 日志
- [ ] 37. `AbstractHttpNotifier` — response 为 null 时加 warn 日志
- [ ] 38. `AbstractDtpNotifier:77,87` — send() 加 try-catch 防止单平台异常中断全部
- [ ] 39. `NotifierHandler:66` — sendNotice 循环加 try-catch per platform
- [ ] 40. `NotifierHandler:78` — sendAlarm 循环加 try-catch per platform
- [ ] 41. `CollectorHandler:63` — collect 循环加 try-catch per collector
- [ ] 42. `AlarmInvoker` — sendAlarm 加 try-catch
- [ ] 43. `NoticeInvoker` — sendNotice 加 try-catch
- [ ] 44. `AlarmManager` — ALARM_EXECUTOR DiscardOldestPolicy 加日志回调
- [ ] 45. `NoticeManager` — NOTICE_EXECUTOR 同上
- [ ] 46. `EventBusManager:58` — post() 加异常处理或注册 DeadEvent handler

### P3 — Adapter / Starter / JVMTI 静默失败日志

- [ ] 47. `ApacheDubboDtpAdapter:116` — 反射字段为 null 时加 warn
- [ ] 48. `ApacheDubboDtpAdapter:141` — handlers 为空时加 debug
- [ ] 49. `LiteflowDtpAdapter:78` — 反射结果 null 检查 + warn
- [ ] 50. `Okhttp3DtpAdapter:74` — 反射字段均失败时加 warn
- [ ] 51. `StarlightServerDtpAdapter:64` — bean 类型不匹配时加 debug
- [ ] 52. `StarlightServerDtpAdapter:72` — uri 为 null 时加 debug
- [ ] 53. `StarlightServerDtpAdapter:76` — processor 为 null 时加 debug
- [ ] 54. `StarlightServerDtpAdapter:80` — threadPoolFactory 为 null 时加 debug
- [ ] 55. `StarlightClientDtpAdapter:72` — threadPoolFactory 为 null 时加 debug
- [ ] 56. `DtpEndpoint.invoke()` — 加 try-catch 防止单 executor 异常导致端点失败
- [ ] 57. `TomcatExecutorProxy:67` — catch(Throwable) 反射降级加 log.debug
- [ ] 58. `EtcdUtil.resultMap` — HashMap 改为 ConcurrentHashMap
- [ ] 59. `JVMTI.getInstances` — 不可用时加 warn
- [ ] 60. `NativeUtil` — 加加载成功/失败日志
- [ ] 61. `JVMTIUtil` — 未知 OS 时加 warn
- [ ] 62. `OSUtils` — 未知 arch 时加 debug

### P3 — 其他日志补全

- [x] 63. `ContextManagerHelper:38-40` — 修复死代码
- [ ] 64. `CollectorHandler:64` — 未知 collectorType 加 warn
- [ ] 65. `NotifierHandler:66` — sendNotice 未知平台加 warn
- [ ] 66. `NotifierHandler:78` — sendAlarm 未知平台加 warn
- [ ] 67. `ExtensionServiceLoader` — 捕获 ServiceConfigurationError
- [ ] 68. `ExtensionServiceLoader` — SPI 结果为空时加 debug
- [ ] 69. `DtpInterceptorRegistry` — 无注解拦截器加 warn
- [ ] 70. `DtpRegistry:117` — putIfAbsent 返回非 null 时加 warn
- [ ] 71. `TaskWrappers.getByNames` — 未识别 wrapper 名称加 warn
- [ ] 72. `RejectedInvocationHandler` — catch 加 log.warn
- [ ] 73. `OrderedDtpExecutor` — 两处 catch 加 log.warn
- [ ] 74. `EagerDtpExecutor:108` — InterruptedException 加 log.warn
- [ ] 75. `DtpLifecycle` — start/stop 加 log.info；shutdownInternal 加 try-catch per destroy

### P4 — 补充单测

- [x] 76. `JMXCollector` — 单测覆盖 GAUGE_CACHE 行为（注册失败不缓存）
- [x] 77. `HashedExecutorSelector` — 单测覆盖负数 hashCode 场景
- [x] 78. `DtpLarkNotifier` — 补充单测
- [x] 79. `DtpDingNotifier` — 补充单测
- [x] 80. `NotifyHelper` — 补充 getNotifyItem/getPlatform 单测
- [ ] 81. `NoticeManager` — 补充单测
- [x] 82. `AlarmInvoker` — 补充单测
- [x] 83. `NoticeInvoker` — 补充单测
- [x] 84. `AlarmLimiter` — 补充单测
- [x] 85. `QueueTimeoutTimerTask` — 补充单测
- [ ] 86. `RunTimeoutTimerTask` — 补充单测
- [ ] 87. `CpuMetricsCaptor` — 补充单测
- [ ] 88. `MemoryMetricsCaptor` — 补充单测
- [ ] 89. `SystemMetricManager` — 补充单测
- [ ] 90. `ExecutorConverter.toMetrics` — 补充边界场景
- [ ] 91. `ContextManagerHelper` — 补充单测
- [ ] 92. `DtpInterceptorRegistry` — 补充单测
- [ ] 93. `JacksonParser` — 补充 Jackson 特有路径单测
- [ ] 94. `FastJsonParser` — 补充单测
- [ ] 95. `GsonParser` — 补充单测
- [ ] 96. `MethodUtil` — 补充 3 个 invoke 方法 catch 路径
- [ ] 97. `DtpLog4j2Logging` — 补充单测
- [ ] 98. `DtpLoggingInitializer` — 补充错误分支单测
- [ ] 99. `DtpLogbackLogging` — 补充错误分支单测
- [ ] 100. `OSUtils` — 补充 normalizeArch 参数化单测
- [ ] 101. `JVMTIUtil` — 补充 OS 检测单测
- [ ] 102. `NativeUtil` — 补充资源提取单测
- [ ] 103. `JVMTI` — 补充 getInstance/getInstances 单测
- [ ] 104. `AlibabaDubboDtpAdapter` — 补充单测
- [ ] 105. `ApacheDubboDtpAdapter` — 修复现有测试，实际导入适配器类
- [ ] 106. `EmailNotifier` — 补充单测
- [ ] 107. `DtpEmailNotifier` — 补充单测
- [ ] 108. `YunZhiJiaNotifier` — 补充单测
- [ ] 109. `DtpYunZhiJiaNotifier` — 补充单测
- [ ] 110. `OpenTelemetryWrapper` — 补充单测
- [ ] 111. `SwTraceTaskWrapper` — 补充单测
- [ ] 112. `EtcdUtil` — 补充单测（线程安全回归保护）
- [ ] 113. `CuratorUtil` — 补充单测
- [ ] 114. `DtpEndpoint` — 补充单测
- [ ] 115. `SpringBootPropertiesBinder` — 补充单测

### P5 — 代码清理

- [ ] 116. 移除 15 个未使用的 `@Slf4j` 注解（见第十八节 129-143）
- [ ] 117. `OSUtils` — platform/arch 加 final 或 volatile
- [ ] 118. `NativeUtil.loadLibraryFromJar` — 加 synchronized
- [ ] 119. `JVMTI.forceGc` — 加 AVAILABLE 检查守卫
