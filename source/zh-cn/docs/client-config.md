title: 客户端配置
------------

## 1. 基础配置

### 1.1 传输协议

`mrpc` 支持 `TCP` 和 `HTTP` 双协议，默认是 `TCP` 协议，性能高于 `HTTP`。

**Spring配置**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">

    <mrpc:clientConfig transport="http"/>

</beans>
```

**SpringBoot配置**

```properties
mrpc.client.transport=http
```

{% note info 客户端和服务端必须保持一致 %}
请确保您的客户端和服务端使用协议是一致的，否则无法进行通信。
{% endnote %}

### 1.2 序列化

`mrpc` 目前支持 `kyro`、`protostuff` 两种序列化协议，默认使用 `kyro`。

**Spring配置**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">

    <mrpc:clientConfig serialize="protostuff"/>

</beans>
```

**SpringBoot配置**

```properties
mrpc.client.serialize=protostuff
```

{% note info 注意添加依赖 %}
修改序列化配置后请查看依赖中是否包含相应的序列化实现。
{% endnote %}

### 1.3 PING

客户端定时向服务器发起 `ping` 请求，目前只支持 `HTTP` 协议，默认不开启，单位毫秒。

**Spring配置**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">

    <mrpc:clientConfig pingInterval="10000"/>

</beans>
```

**SpringBoot配置**

```properties
mrpc.client.pingInterval=10000
```

### 1.4 拦截器

### 1.5 直连服务端

当您不需要分布式的时候或是单机多节点的伪集群可以使用直连服务端的方式进行配置，多个节点用逗号相隔。

**Spring配置**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">

    <mrpc:clientConfig directAddress="127.0.0.1:5066"/>

</beans>
```

**SpringBoot配置**

```properties
mrpc.client.directAddress=127.0.0.1:5066
```

### 1.6 调用超时

当服务端执行某个方法超时，客户端可配置超时时长，默认为10秒，超过该时间会抛出 `TimeoutException` 异常，可修改。

**Spring配置**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">

    <mrpc:clientConfig waitTimeout="20000"/>

</beans>
```

**SpringBoot配置**

```properties
mrpc.client.waitTimeout=20000
```

## 2. 负载均衡

`mrpc` 目前提供三种负载均衡策略，默认为 `ROUND`。

- `ROUND`: 顺序轮询，客户端轮询调用服务节点列表
- `RANDOM`: 随机，客户端随机选择一个节点调用
- `CALLLEAST`: 最少调用，客户端选择当前服务列表中最少被调用的节点（压力最小的服务）

### 2.1 Spring配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">

    <mrpc:clientConfig lbStrategy="RANDOM"/>

</beans>
```

### 2.2 SpringBoot配置

```properties
mrpc.client.lbStrategy=RANDOM
```

## 3. 高可用

`mrpc` 提供两种高可用策略，快速失败(Failfast)和失效切换(Failover)，默认为失效切换，重试3次。

- `Failfast`: 如果Client调用失败，立即返回，不会重试
- `Failover`: 如果Client调用失败且有多个服务，会尝试从服务列表中选择另外一个服务器调用，直到成功或者到达重试次数

### 3.1 Spring配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">

    <mrpc:clientConfig haStrategy="failfast" failOverRetry="5"/>

</beans>
```

### 3.2 SpringBoot配置

```properties
mrpc.client.haStrategy=failfast
mrpc.client.failOverRetry=5
```
