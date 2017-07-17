title: 服务端配置
------------

## 1. 基础配置

### 1.1 暴露地址

作为 `rpc` 服务端，需将服务绑定在某个进程上运行，因为通过网络进行通信，所以这里需要暴露一个 `ip:port` 出去。

**Spring配置**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">
    
    <mrpc:serverConfig address="127.0.0.1:5067" />
    
</beans>
```

**SpringBoot配置**

```properties
mrpc.server.address=127.0.0.1:5067
```

{% note info 自动识别IP地址 %}
当您在进行分布式部署的时候可能不想修改多份配置，修改 `mrpc.server.address`。
可以只填写端口，框架会自动绑定服务器的第一块网卡的IP地址，也可以在运行时添加参数 `--mrpc.server.address=192.168.2.112:5067`
{% endnote %}

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
    
    <mrpc:serverConfig transport="http" address="127.0.0.1:5067" />
    
</beans>
```

**SpringBoot配置**

```properties
mrpc.server.transport=http
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

    <mrpc:serverConfig serialize="protostuff" address="127.0.0.1:5067" />
       
</beans>
```

**SpringBoot配置**

```properties
mrpc.server.serialize=protostuff
```

{% note info 注意添加依赖 %}
修改序列化配置后请查看依赖中是否包含相应的序列化实现。
{% endnote %}

## 2. 服务器权重

当您用到加权负载均衡的时候，可以为不同服务器分配权重，根据资源占用等情况进行调整。

**Spring配置**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">
    
    <mrpc:serverConfig address="127.0.0.1:5067" weight="2"/>
    
</beans>
```

**SpringBoot配置**

```properties
mrpc.server.weight=2
```

