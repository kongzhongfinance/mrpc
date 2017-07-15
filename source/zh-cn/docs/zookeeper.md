title: Zookeeper注册中心
--------------------
## 1. Spring配置

### 1.1 服务端

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">

    <!-- zookeeper注册中心，集群用逗号隔开 -->
    <mrpc:registry name="default" type="zookeeper" address="127.0.0.1:2181"/>

    <mrpc:serverConfig address="127.0.0.1:5066" appId="demo" />

    <context:component-scan base-package="com.exmaple.api.service"/>

</beans>
```

这里配置了一个注册中心，名称为 `default` 所有的服务默认都会使用 `default` 注册中心。
启动服务端后在 `zookeeper` 中查看已经存在一个节点 `/mrpc/demo/com.exmaple.api.service.UserService/127.0.0.1:5066`;
`appId` 为了区分不同项目所在分组。

### 1.2 客户端

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">

    <mrpc:clientConfig appId="demo"/>

    <mrpc:registry name="default" type="zookeeper" address="127.0.0.1:2181"/>

    <mrpc:referer id="userService" interface="com.kongzhong.mrpc.demo.service.UserService"/>

</beans>
```

客户端配置从 `zookeeper` 拉取服务配置。

## 2. SpringBoot配置

### 2.1 服务端

```properties
mrpc.server.appId=demo
mrpc.server.address=127.0.0.1:5066
mrpc.registry[default].type=zookeeper
mrpc.registry[default].address=127.0.0.1:2181
logging.level.com.kongzhong.mrpc=debug
```

### 2.2 客户端

```properties
mrpc.client.appId=demo
mrpc.registry[default].type=zookeeper
mrpc.registry[default].address=127.0.0.1:2181
logging.level.com.kongzhong.mrpc=debug
```