title: 开发环境
--------------------

`mrpc` 基于java8开发，使用 `maven` 进行构建，请确保您的JDK环境为1.8。

## 1. 核心依赖

- `netty4`：网络通讯库
- `guava`：基础工具包
- `jackson`：JSON序列化库
- `slf4j-api`：日志组件接口，自行指定日志实现

## 2. POM依赖

```xml
<!-- RPC核心 -->
<dependency>
    <groupId>com.kongzhong.mrpc</groupId>
    <artifactId>mrpc-core</artifactId>
    <version>[最新版本]</version>
</dependency>

<!-- RPC序列化[可更换] -->
<dependency>
    <groupId>com.kongzhong.mrpc</groupId>
    <artifactId>mrpc-serialize-kryo</artifactId>
    <version>[最新版本]</version>
</dependency>

<!-- RPC JSON解析 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.8.7</version>
</dependency>

<!-- RPC注册中心,可选 -->
<dependency>
    <groupId>com.kongzhong.mrpc</groupId>
    <artifactId>mrpc-registry-zk</artifactId>
    <version>[最新版本]</version>
</dependency>

<!-- 日志,根据需要 -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>1.7.24</version>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.2.3</version>
</dependency>
```
