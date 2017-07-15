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

当服务端执行某个方法超时，客户端可配置超时等待时长，默认为10秒，单位毫秒，超过该时间会抛出 `TimeoutException` 异常。

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

`mrpc` 实现了服务器软负载，讲述的是"将外部发送来的请求均匀分配到对称结构中的某一台服务器上"的各种算法。

目前提供五种负载均衡策略，默认为 `ROUND`（轮循）。

1、 `ROUND`: 顺序轮循，客户端轮循调用服务节点列表

> 轮询调度算法的原理是每一次把来自用户的请求轮流分配给内部中的服务器，从1开始，直到N(内部服务器个数)，然后重新开始循环。算法的优点是其简洁性，它无需记录当前所有连接的状态，所以它是一种无状态调度。

2、 `WEIGHT_ROUND`: 加权轮询

> 不同的后端服务器可能机器的配置和当前系统的负载并不相同，因此它们的抗压能力也不相同。给配置高、负载低的机器配置更高的权重，让其处理更多的请；而配置低、负载高的机器，给其分配较低的权重，降低其系统负载，加权轮询能很好地处理这一问题，并将请求顺序且按照权重分配到后端。

3、`WEIGHT_RANDOM`: 加权随机

> 与加权轮询法一样，加权随机法也根据后端机器的配置，系统的负载分配不同的权重。不同的是，它是按照权重随机请求后端服务器，而非顺序。

4、 `RANDOM`: 随机，客户端随机选择一个节点调用

> 通过系统的随机算法，根据后端服务器的列表大小值来随机选取其中的一台服务器进行访问。由概率统计理论可以得知，随着客户端调用服务端的次数增多，
> 其实际效果越来越接近于平均分配调用量到后端的每一台服务器，也就是轮询的结果。

5、 `CALLLEAST`: 最少调用，客户端选择当前服务列表中最少被调用的节点（压力最小的服务）

> 系统把新连接分配给当前连接数目最少的服务器。该算法在各个服务器运算能力基本相似的环境中非常有效。

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

