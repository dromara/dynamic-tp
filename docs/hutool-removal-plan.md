# Hutool 剥离方案

> 目标：将 dynamic-tp 项目对 `cn.hutool`（hutool-core 5.8.25 + hutool-http 5.8.25）的全部依赖移除，替换为 JDK 原生 API、项目已有依赖或少量自建工具方法。

## 一、现状概览

### 1.1 依赖声明位置

| 文件 | 声明内容 |
|------|----------|
| [dependencies/pom.xml](../dependencies/pom.xml#L23) | `<hutool.version>5.8.25</hutool.version>` 版本属性 |
| [dependencies/pom.xml](../dependencies/pom.xml#L115-L125) | `dependencyManagement` 中管理 `hutool-core` 和 `hutool-http` |
| [common/pom.xml](../common/pom.xml#L49-L52) | `common` 模块直接依赖 `hutool-http` |
| [core/pom.xml](../core/pom.xml#L40-L43) | `core` 模块直接依赖 `hutool-core` |

### 1.2 使用统计

- **主代码**：13 个文件，涉及 6 类 Hutool API
- **测试代码**：6 个文件，涉及 4 类 Hutool API
- **共计**：19 个文件

### 1.3 项目约束

- **Java 版本：Java 8**（`maven.compiler.source/target = 8`），**不能使用** `java.net.http.HttpClient`（Java 11+）
- `common` 模块和 `core` 模块**未直接依赖 Spring**，设计上保持轻量解耦，替换方案应避免向这两个模块引入 Spring 依赖

### 1.4 可用已有库

以下库已在项目依赖中，替代方案**优先复用**，避免重复造轮子：

| 库 | 坐标 | 本方案用到的 API |
|----|------|-----------------|
| **Apache Commons Lang3** | `org.apache.commons:commons-lang3` | `ArrayUtils.isNotEmpty()`、`FieldUtils`（支撑自建 BeanUtil）、已有 `ReflectionUtil` |
| **Guava** | `com.google.guava:guava` | `Sets.newHashSet()`（测试）、`Files.asCharSource().read()`（测试可选） |
| **Apache Commons Collections4** | `org.apache.commons:commons-collections4` | `CollectionUtils`（已有使用） |
| **commons-codec** | `commons-codec:commons-codec` | 已有的 `Base64` 等编解码（LarkNotifier 已用） |
| **Lombok** | `org.projectlombok:lombok` | `@Slf4j`、`val` 等（已有使用） |

---

## 二、Hutool API 使用清单与替代方案

### 2.1 主代码（src/main）

#### ① `CharSequenceUtil.format()` — `{}` 占位符字符串格式化（3 处）

| 文件 | 行号 | 用法 |
|------|------|------|
| [RunTimeoutTimerTask.java](../core/src/main/java/org/dromara/dynamictp/core/timer/RunTimeoutTimerTask.java#L51) | 51 | `CharSequenceUtil.format("... {}, ...", args)` |
| [QueueTimeoutTimerTask.java](../core/src/main/java/org/dromara/dynamictp/core/timer/QueueTimeoutTimerTask.java#L48) | 48 | 同上 |
| [TaskRejectAware.java](../core/src/main/java/org/dromara/dynamictp/core/aware/TaskRejectAware.java#L62) | 62 | 同上 |

**替代方案**：在 `common` 模块新建 `StrUtil.format()` 自实现 `{}` 占位符替换

> ~~SLF4J 的 `MessageFormatter`~~ **不适用**——其对最后一个参数为 `Throwable` 时有特殊处理（不替换 `{}` 而提取为异常），且对 `\{}` 有转义语义，与 Hutool 的 `CharSequenceUtil.format()` 行为不完全一致。自建极简方法可保证行为完全等价，零依赖。

```java
// Before
String logMsg = CharSequenceUtil.format("tpName: {}, taskName: {}", name, taskName);

// After
import org.dromara.dynamictp.common.util.StrUtil;
String logMsg = StrUtil.format("tpName: {}, taskName: {}", name, taskName);
```

```java
// common/src/main/java/org/dromara/dynamictp/common/util/StrUtil.java
public final class StrUtil {

    private static final String PLACEHOLDER = "{}";

    private StrUtil() {}

    /**
     * 使用 {} 占位符格式化字符串，行为与 Hutool CharSequenceUtil.format 完全一致。
     */
    public static String format(String template, Object... args) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        if (args == null || args.length == 0) {
            return template;
        }
        StringBuilder sb = new StringBuilder(template.length() + 50);
        int argIndex = 0;
        int i = 0;
        while (i < template.length()) {
            if (i < template.length() - 1
                    && template.charAt(i) == '{'
                    && template.charAt(i + 1) == '}') {
                if (argIndex < args.length) {
                    sb.append(args[argIndex++]);
                } else {
                    sb.append(PLACEHOLDER);
                }
                i += 2;
            } else {
                sb.append(template.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }
}
```

---

#### ② `BeanUtil.copyProperties()` — Bean 属性拷贝（4 处）

| 文件 | 行号 | 拷贝对象 |
|------|------|----------|
| [ExecutorWrapper.java](../core/src/main/java/org/dromara/dynamictp/core/support/ExecutorWrapper.java#L169) | 169 | `ExecutorWrapper` → `ExecutorWrapper` |
| [AbstractDtpNotifier.java](../core/src/main/java/org/dromara/dynamictp/core/notifier/AbstractDtpNotifier.java#L187) | 187 | `NotifyPlatform` → `NotifyPlatform` |
| [MicroMeterCollector.java](../core/src/main/java/org/dromara/dynamictp/core/monitor/collector/MicroMeterCollector.java#L65) | 65 | `ThreadPoolStats` → `ThreadPoolStats` |
| [JMXCollector.java](../core/src/main/java/org/dromara/dynamictp/core/monitor/collector/jmx/JMXCollector.java#L52) | 52 | `ThreadPoolStats` → `ThreadPoolStats` |

**替代方案**：在 `common` 模块新建 `BeanUtil` 工具类，基于反射实现简单属性拷贝

> 由于 `common`/`core` 模块无 Spring 依赖，不宜使用 `org.springframework.beans.BeanUtils`。四个场景均为**同类型**对象拷贝，简单的反射实现完全够用。项目已有 `ReflectionUtil`（基于 Apache Commons Lang3 `FieldUtils`），可复用其底层。

```java
// common/src/main/java/org/dromara/dynamictp/common/util/BeanUtil.java
public final class BeanUtil {
    private BeanUtil() {}

    public static void copyProperties(Object source, Object target) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
        for (Field field : FieldUtils.getAllFieldsList(source.getClass())) {
            try {
                Object value = FieldUtils.readField(field, source, true);
                Field targetField = FieldUtils.getField(target.getClass(), field.getName(), true);
                if (targetField != null) {
                    FieldUtils.writeField(targetField, target, value, true);
                }
            } catch (IllegalAccessException e) {
                // skip unreadable/unwritable field
            }
        }
    }
}
```

---

#### ③ `NumberUtil.div()` — 除法保留小数（1 文件，2 处）

| 文件 | 行号 | 用法 |
|------|------|------|
| [AlarmManager.java](../core/src/main/java/org/dromara/dynamictp/core/notifier/manager/AlarmManager.java#L160) | 160 | `NumberUtil.div(activeCount, maxPoolSize, 2) * 100` |
| [AlarmManager.java](../core/src/main/java/org/dromara/dynamictp/core/notifier/manager/AlarmManager.java#L174) | 174 | `NumberUtil.div(queueSize, queueCapacity, 2) * 100` |

**替代方案**：`BigDecimal` 除法

```java
// Before
double div = NumberUtil.div(executor.getActiveCount(), maximumPoolSize, 2) * 100;

// After
double div = BigDecimal.valueOf(executor.getActiveCount())
        .divide(BigDecimal.valueOf(maximumPoolSize), 2, RoundingMode.HALF_UP)
        .doubleValue() * 100;
```

---

#### ④ `UrlBuilder` — URL 构建与查询参数操作（4 处）

| 文件 | 用法 |
|------|------|
| [DingNotifier.java](../common/src/main/java/org/dromara/dynamictp/common/notifier/DingNotifier.java#L90) | `UrlBuilder.of(webhook)` → `addQuery()` → `build()` |
| [LarkNotifier.java](../common/src/main/java/org/dromara/dynamictp/common/notifier/LarkNotifier.java#L103) | `UrlBuilder.of()` → `getPath().getSegments()` → `addPath()` → `build()` |
| [WechatNotifier.java](../common/src/main/java/org/dromara/dynamictp/common/notifier/WechatNotifier.java#L60) | `UrlBuilder.of()` → `getQuery().get()` → `addQuery()` → `build()` |
| [YunZhiJiaNotifier.java](../extension/extension-notify-yunzhijia/src/main/java/org/dromara/dynamictp/extension/notify/yunzhijia/YunZhiJiaNotifier.java#L58) | 同 Wechat 模式 |

**替代方案**：在 `common` 模块新建轻量 `UrlBuilder` 工具类

> 使用 JDK `java.net.URI` / `java.net.URL` 解析，`StringBuilder` 拼接查询参数，覆盖当前所有使用场景（addQuery、getQuery、getPath segments、addPath）。

```java
// common/src/main/java/org/dromara/dynamictp/common/util/UrlBuilder.java
public class UrlBuilder {
    private final String base;
    private final Map<String, String> queryParams = new LinkedHashMap<>();
    private final List<String> pathSegments = new ArrayList<>();

    private UrlBuilder(String base) {
        // 解析已有 query 和 path 到各自集合
        this.base = base;
    }

    public static UrlBuilder of(String url) { return new UrlBuilder(url); }
    public String getQuery(String key) { return queryParams.get(key); }
    public UrlBuilder addQuery(String key, Object val) { queryParams.put(key, String.valueOf(val)); return this; }
    public List<String> getPathSegments() { return pathSegments; }
    public UrlBuilder addPath(String segment) { pathSegments.add(segment); return this; }
    public String build() { /* 拼装最终 URL */ }
}
```

---

#### ⑤ `HttpRequest` / `HttpResponse` — HTTP POST（1 处，核心改动）

| 文件 | 行号 | 用法 |
|------|------|------|
| [AbstractHttpNotifier.java](../common/src/main/java/org/dromara/dynamictp/common/notifier/AbstractHttpNotifier.java#L43-L54) | 43-54 | `HttpRequest.post(url).setConnectionTimeout(t).setReadTimeout(t).body(msgBody).setProxy(...).execute()` |

**替代方案**：JDK 原生 `HttpURLConnection`（零新增依赖）

> 当前 HTTP 调用非常简单——仅 POST JSON + 超时 + 代理，没有连接池、拦截器、异步、文件上传等复杂能力。`HttpURLConnection` 完全胜任，无需引入任何第三方 HTTP 客户端，符合"减少第三方依赖"的剥离目标。

```java
// AbstractHttpNotifier.send0() 改写
@Override
protected void send0(NotifyPlatform platform, String content) {
    String url = buildUrl(platform);
    String msgBody = buildMsgBody(platform, content);

    try {
        URL targetUrl = new URL(url);
        Proxy proxy = platform.getProxyType() != Proxy.Type.DIRECT
                ? new Proxy(platform.getProxyType(), new InetSocketAddress(platform.getProxyHost(), platform.getProxyPort()))
                : Proxy.NO_PROXY;

        HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection(proxy);
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(platform.getTimeout());
        conn.setReadTimeout(platform.getTimeout());
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(msgBody.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        String respBody = readBody(conn, code);
        log.info("DynamicTp notify, {} send success, response: {}, request: {}",
                platform(), respBody, msgBody);
    } catch (IOException e) {
        log.error("DynamicTp notify, {} send failed, request: {}", platform(), msgBody, e);
    }
}

private String readBody(HttpURLConnection conn, int code) throws IOException {
    InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
    if (is == null) {
        return "";
    }
    try (InputStream in = is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }
}
```

---

#### ⑥ `ReflectUtil` — 反射获取/设置字段（1 文件，多处）

| 文件 | 行号 | 用法 |
|------|------|------|
| [DtpPropertiesBinderUtil.java](../common/src/main/java/org/dromara/dynamictp/common/util/DtpPropertiesBinderUtil.java#L92) | 92 | `ReflectUtil.getFieldValue(dtpProperties, field)` |
| 同上 | 149 | `ReflectUtil.setFieldValue(executor, fieldName, value)` |
| 同上 | 155,159,163,167,172 | 多处 `ReflectUtil.setFieldValue(...)` |

**替代方案**：**直接使用项目已有的 `ReflectionUtil`**（[common/.../ReflectionUtil.java](../common/src/main/java/org/dromara/dynamictp/common/util/ReflectionUtil.java)）

> 项目已封装了 `ReflectionUtil`，基于 Apache Commons Lang3 `FieldUtils`，提供了 `getFieldValue(fieldName, obj)` 和 `setFieldValue(fieldName, obj, val)` 方法。Hutool 的 `ReflectUtil.getFieldValue(obj, field)` 和 `ReflectUtil.setFieldValue(obj, fieldName, value)` 参数顺序不同，需注意调整。

```java
// Before
ReflectUtil.getFieldValue(dtpProperties, dtpPropertiesField)
ReflectUtil.setFieldValue(executor, field.getName(), globalFieldVal)

// After — 参数顺序：fieldName 在前，obj 在后
ReflectionUtil.getFieldValue(dtpPropertiesField.getName(), dtpProperties)
ReflectionUtil.setFieldValue(field.getName(), executor, globalFieldVal)
```

---

#### ⑦ `CollUtil.contains()` / `ArrayUtil.isNotEmpty()` — 集合/数组工具（1 文件，2 处）

| 文件 | 行号 | 用法 |
|------|------|------|
| [AgentAware.java](../extension/extension-agent/src/main/java/org/dromara/dynamictp/extension/agent/AgentAware.java#L75) | 75 | `CollUtil.contains(visitedClass, o.getClass())` |
| [AgentAware.java](../extension/extension-agent/src/main/java/org/dromara/dynamictp/extension/agent/AgentAware.java#L92) | 92 | `ArrayUtil.isNotEmpty(declaredFields)` |

**替代方案**：JDK 原生 + Apache Commons（已有）

```java
// CollUtil.contains → Collection.contains (或 Apache Commons CollectionUtils.contains)
visitedClass.contains(o.getClass())

// ArrayUtil.isNotEmpty → Apache Commons ArrayUtils.isNotEmpty (已有依赖)
import org.apache.commons.lang3.ArrayUtils;
ArrayUtils.isNotEmpty(declaredFields)
```

---

#### ⑧ `FileUtil.readableFileSize()` — 字节数转可读大小（1 处）

| 文件 | 行号 | 用法 |
|------|------|------|
| [DtpEndpoint.java](../starter/starter-common/src/main/java/org/dromara/dynamictp/starter/common/monitor/DtpEndpoint.java#L60-L63) | 60-63 | `FileUtil.readableFileSize(runtime.maxMemory())` |

**替代方案**：自建工具方法（`starter-common` 模块已有 Spring 依赖）

```java
private static String readableFileSize(long bytes) {
    if (bytes < 1024) return bytes + " B";
    int unitIndex = (int) (Math.log(bytes) / Math.log(1024));
    String[] units = {"B", "KB", "MB", "GB", "TB"};
    double value = bytes / Math.pow(1024, unitIndex);
    return String.format("%.2f %s", value, units[unitIndex]);
}
```

> 也可放入 `common` 模块的 `FileSizeUtil` 中统一管理。

---

### 2.2 测试代码（src/test）

| 文件 | Hutool API | 替代方案 |
|------|------------|----------|
| [InterceptTest.java](../test/test-core/src/test/java/org/dromara/dynamictp/test/core/plugin/InterceptTest.java#L58) | `CollectionUtil.newHashSet("TestAInterceptor")` | **Guava** `Sets.newHashSet("TestAInterceptor")` |
| [YamlConfigParserTest.java](../test/test-core/src/test/java/org/dromara/dynamictp/test/core/parse/YamlConfigParserTest.java#L46) | `FileUtil.readString(file, charset)` | JDK `new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)` |
| [PropertiesConfigParserTest.java](../test/test-core/src/test/java/org/dromara/dynamictp/test/core/parse/PropertiesConfigParserTest.java#L46) | 同上 | 同上 |
| [JsonConfigParserTest.java](../test/test-core/src/test/java/org/dromara/dynamictp/test/core/parse/JsonConfigParserTest.java#L46) | 同上 | 同上 |
| [DtpWechatNotifierTest.java](../test/test-core/src/test/java/org/dromara/dynamictp/test/core/notify/DtpWechatNotifierTest.java#L20-L21) | `HttpRequest`, `HttpResponse`（import 但**未使用**） | 直接删除 import |
| [BeanCopierUtilTest.java](../test/test-common/src/test/java/org/dromara/dynamictp/test/common/util/BeanCopierUtilTest.java#L51) | `BeanUtil.copyProperties(src, target)` | 使用项目新建的 `BeanUtil.copyProperties()`（基于 **commons-lang3** `FieldUtils`） |

---

## 三、实施计划

### Phase 1：新建替代工具类（无破坏性变更）

| 步骤 | 模块 | 内容 |
|------|------|------|
| 1.1 | `common` | 新建 `StrUtil.java` — 自实现 `{}` 占位符格式化（无已有库可替代） |
| 1.2 | `common` | 新建 `BeanUtil.java` — 基于 **commons-lang3** `FieldUtils` 的属性拷贝 |
| 1.3 | `common` | 新建 `UrlBuilder.java` — 轻量 URL 构建器（无已有库可替代） |
| 1.4 | `common` | 新建 `FileSizeUtil.java`（可选）— 字节数转可读字符串 |

### Phase 2：替换主代码引用

按依赖关系由底向上替换，每个模块替换后编译验证：

| 步骤 | 模块 | 文件 | 替换内容 |
|------|------|------|----------|
| 2.1 | `common` | DtpPropertiesBinderUtil.java | `ReflectUtil` → 项目已有 `ReflectionUtil` |
| 2.2 | `common` | DingNotifier, LarkNotifier, WechatNotifier | `UrlBuilder` → 项目新建 `UrlBuilder` |
| 2.3 | `common` | AbstractHttpNotifier.java | `HttpRequest/HttpResponse` → `HttpURLConnection` |
| 2.4 | `common/pom.xml` | — | 移除 `hutool-http` 依赖（**无需新增依赖**） |
| 2.5 | `core` | RunTimeoutTimerTask, QueueTimeoutTimerTask, TaskRejectAware | `CharSequenceUtil` → `StrUtil` |
| 2.6 | `core` | AlarmManager.java | `NumberUtil.div` → `BigDecimal` |
| 2.7 | `core` | ExecutorWrapper, AbstractDtpNotifier, MicroMeterCollector, JMXCollector | `BeanUtil` → 项目新建 `BeanUtil` |
| 2.8 | `core/pom.xml` | — | 移除 `hutool-core` 依赖 |
| 2.9 | `extension-agent` | AgentAware.java | `CollUtil.contains` → `contains()`，`ArrayUtil.isNotEmpty` → `ArrayUtils.isNotEmpty` |
| 2.10 | `extension-notify-yunzhijia` | YunZhiJiaNotifier.java | `UrlBuilder` → 项目新建 `UrlBuilder` |
| 2.11 | `starter-common` | DtpEndpoint.java | `FileUtil.readableFileSize` → 自建方法 |

### Phase 3：替换测试代码引用

| 步骤 | 文件 | 替换内容 |
|------|------|----------|
| 3.1 | InterceptTest.java | `CollectionUtil.newHashSet` → **Guava** `Sets.newHashSet` |
| 3.2 | YamlConfigParserTest.java | `FileUtil.readString` → `Files.readAllBytes` |
| 3.3 | PropertiesConfigParserTest.java | 同上 |
| 3.4 | JsonConfigParserTest.java | 同上 |
| 3.5 | DtpWechatNotifierTest.java | 删除未使用的 import |
| 3.6 | BeanCopierUtilTest.java | `BeanUtil.copyProperties` → 项目新建 `BeanUtil` |

### Phase 4：清理依赖

| 步骤 | 文件 | 内容 |
|------|------|------|
| 4.1 | [dependencies/pom.xml](../dependencies/pom.xml) | 移除 `<hutool.version>` 属性 |
| 4.2 | [dependencies/pom.xml](../dependencies/pom.xml) | 移除 `dependencyManagement` 中 `hutool-core` 和 `hutool-http` |
| 4.3 | — | **无需新增依赖**（HTTP 改用 JDK 原生 `HttpURLConnection`） |
| 4.4 | 全局 | `grep -r "hutool"` 确认无残留引用 |

### Phase 5：验证

- [ ] `mvn clean compile -pl common,core,extension/extension-agent,extension/extension-notify-yunzhijia,starter/starter-common` 编译通过
- [ ] `mvn test` 全部测试通过
- [ ] `grep -r "cn.hutool"` 返回空
- [ ] 检查无 `hutool` 的传递依赖：`mvn dependency:tree | grep hutool`

---

## 四、风险评估

| 风险点 | 等级 | 说明 | 缓解措施 |
|--------|------|------|----------|
| `HttpURLConnection` 替换 HTTP 行为差异 | **低** | hutool-http 默认不抛异常，`HttpURLConnection` 对 4xx/5xx 需读取 `getErrorStream()` | `readBody()` 方法已按状态码分流处理；需确认日志输出格式不变 |
| `BeanUtil.copyProperties` 行为差异 | **中** | Hutool 版支持类型转换、忽略 null 等选项 | 当前 4 处使用均为同类型简单属性拷贝、未使用 CopyOptions，简单反射实现完全等价 |
| `UrlBuilder` 边界场景 | **低** | URL 编码、特殊字符处理 | 现有 webhook URL 结构简单，测试用例覆盖正常场景即可 |
| `ReflectionUtil` 参数顺序 | **低** | Hutool `ReflectUtil.getFieldValue(obj, field)` vs 项目 `ReflectionUtil.getFieldValue(fieldName, obj)` | 逐处替换时仔细调整参数顺序，编译器会捕获类型不匹配 |
| `NumberUtil.div` 除零保护 | **低** | Hutool 的 div 有除零保护（返回 0） | AlarmManager 中 `maximumPoolSize` 和 `queueCapacity` 不会为 0；如有顾虑加 `if (divisor == 0) return 0;` |

---

## 五、新增工具类清单

| 类名 | 模块 | 位置 | 替代 API |
|------|------|------|----------|
| `StrUtil` | common | `common/src/main/java/org/dromara/dynamictp/common/util/StrUtil.java` | `CharSequenceUtil.format` |
| `BeanUtil` | common | `common/src/main/java/org/dromara/dynamictp/common/util/BeanUtil.java` | `cn.hutool.core.bean.BeanUtil` |
| `UrlBuilder` | common | `common/src/main/java/org/dromara/dynamictp/common/util/UrlBuilder.java` | `cn.hutool.core.net.url.UrlBuilder` |
| `FileSizeUtil`（可选） | common | `common/src/main/java/org/dromara/dynamictp/common/util/FileSizeUtil.java` | `cn.hutool.core.io.FileUtil.readableFileSize` |

> 以上工具类均放在 `common` 模块的 `org.dromara.dynamictp.common.util` 包下，与已有的 `ReflectionUtil`、`JsonUtil` 等保持一致。

---

## 六、依赖变更汇总

### 移除

```xml
<!-- dependencies/pom.xml -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-core</artifactId>
    <version>${hutool.version}</version>   <!-- 移除 -->
</dependency>
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-http</artifactId>
    <version>${hutool.version}</version>   <!-- 移除 -->
</dependency>
<!-- 同时移除 <hutool.version>5.8.25</hutool.version> 属性 -->
```

```xml
<!-- common/pom.xml -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-http</artifactId>   <!-- 移除 -->
</dependency>
```

```xml
<!-- core/pom.xml -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-core</artifactId>   <!-- 移除 -->
</dependency>
```

### 新增

**无需新增任何依赖**——HTTP 调用改用 JDK 原生 `HttpURLConnection`，其余功能由已有的 SLF4J、Apache Commons Lang3 等依赖覆盖。
