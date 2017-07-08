title: Hello World（Spring版本）
--------------------
## API

首先定义一个API接口

```java
public interface UserService{
    String hello(String msg);
}
```

## 服务端

### 添加依赖

```xml
<!-- 引入接口定义 -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>example-api</artifactId>
    <version>0.0.1</version>
</dependency>

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

### 服务实现

`mrpc` 支持注解的方式和 `xml` 配置方式将服务暴露出去

#### XML配置方式

首先编写一个服务实现类 `UserServiceImpl`

```java
public class UserServiceImpl implments UserService{
    @Override
    public String hello(String msg){
        return "rpc => " + msg;
    }
}
```

非常简单的一个实现，接下来在 `spring` 配置文件中加入配置:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">

    <mrpc:serverConfig address="127.0.0.1:5066" />

    <bean id="userService" class="com.exmaple.api.service.UserServiceImpl"/>

    <mrpc:service interface="com.exmaple.api.service.UserService" ref="userService"/>

</beans>
```

这里暴露了一个服务 `UserService` 端口为 `5066`

#### 注解方式[推荐]

编写一个实现类

```java
@RpcService
public class UserServiceImpl implments UserService{
    @Override
    public String hello(String msg){
        return "rpc => " + msg;
    }
}
```

配置文件


```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">

    <mrpc:serverConfig address="127.0.0.1:5066" />

    <context:component-scan base-package="com.exmaple.api.service"/>

</beans>
```

### 启动服务端

编写一个 `Java` 类启动服务端:

```java
public class HelloWorldApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-server.xml");
        ctx.registerShutdownHook();
        ctx.start();
    }
}
```

如果你配置了日志输出，并把 `rpc` 的日志级别设置为 `DEBUG` 应该可以看到类似如下输出：

```bash
		    __  _______  ____  ______
		   /  |/  / __ \/ __ \/ ____/
		  / /|_/ / /_/ / /_/ / /
		 / /  / / _, _/ ____/ /___
		/_/  /_/_/ |_/_/    \____/

	　:: mrpc server :: (0.0.9-SNAPSHOT)

2017-07-08 22:10:14.964 [main] INFO  c.k.mrpc.server.SimpleRpcServer | Register => [com.exmaple.api.service.UserService] - [127.0.0.1:5066]
2017-07-08 22:10:14.964 [main] INFO  c.k.mrpc.server.SimpleRpcServer | Publish services finished, mrpc version [0.0.9-SNAPSHOT]
```

## 客户端

### 添加依赖

```xml
<!-- 引入接口定义 -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>example-api</artifactId>
    <version>0.0.1</version>
</dependency>

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

### 客户端调用

#### 硬编码

```java
RpcSpringClient rpcClient = new RpcSpringClient();
rpcClient.setDirectAddress("127.0.0.1:5066");

UserService userService = rpcClient.getProxyReferer(UserService.class);
String result = userService.hello("jack");
System.out.println(result);
```

#### 配置文件

```java
ApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-client.xml");
UserService userService = ctx.getBean(UserService.class);
String result = userService.hello("jack");
System.out.println(result);
```

客户端编写配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mrpc="http://mrpc.kongzhong.com/schema/mrpc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://mrpc.kongzhong.com/schema/mrpc http://mrpc.kongzhong.com/schema/mrpc.xsd">

    <mrpc:clientConfig directAddress="127.0.0.1:5066" />

    <mrpc:referer id="userService" interface="com.kongzhong.mrpc.demo.service.UserService"/>

</beans>
```

这里为了简单起见配置了 `directAddress` 表示客户端直连到服务端,多个可用逗号隔开。

```java
public class SpringClientApplication {

    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-client.xml");
        UserService userService = ctx.getBean(UserService.class);
        String result = userService.hello("jack");
        System.out.println(result);
    }
}
```

### 在控制器中使用

```java
@RestController
public class IndexController{

    @Autowired
    private UserService userService;

    @GetMapping("/hello")
    public String hello(String msg){
        return userService.hello(msg);
    }
}
```
