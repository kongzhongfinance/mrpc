title: Hello World（SpringBoot版本）
--------------------------------
## 1. API

首先定义一个API接口

```java
public interface UserService{
    String hello(String msg);
}
```

## 2. 服务端

### 2.1 添加依赖

```xml
<!-- 引入接口定义 -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>example-api</artifactId>
    <version>0.0.1</version>
</dependency>

<!-- RPC sprint-boot starter -->
<dependency>
    <groupId>com.kongzhong.mrpc</groupId>
    <artifactId>mrpc-spring-boot-starter</artifactId>
    <version>[最新版本]</version>
</dependency>
```

### 2.2 服务实现

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

```properties
mrpc.server.address=127.0.0.1:5066
logging.level.com.kongzhong.mrpc=debug
```

### 2.3 启动服务端

编写一个 `Java` 类启动服务端:

```java
@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
```

运行 `ServerApplication` 启动服务端

```bash
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.5.2.RELEASE)

2017-07-09 13:29:24.278  INFO 71509 --- [           main] c.k.mrpc.server.ServerApplication        : Starting ServerApplication on biezhi.local with PID 71509 (/Users/biezhi/workspace/projects/java/mrpc/mrpc-demo/spring-boot-server/target/classes started by biezhi in /Users/biezhi/workspace/projects/java/mrpc)
2017-07-09 13:29:24.285 DEBUG 71509 --- [           main] c.k.mrpc.server.ServerApplication        : Running with Spring Boot v1.5.2.RELEASE, Spring v4.3.7.RELEASE
2017-07-09 13:29:24.285  INFO 71509 --- [           main] c.k.mrpc.server.ServerApplication        : No active profile set, falling back to default profiles: default
2017-07-09 13:29:24.357  INFO 71509 --- [           main] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@623a8092: startup date [Sun Jul 09 13:29:24 CST 2017]; root of context hierarchy
2017-07-09 13:29:24.799  INFO 71509 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'mrpc-com.kongzhong.mrpc.springboot.config.CommonProperties' of type [com.kongzhong.mrpc.springboot.config.CommonProperties] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2017-07-09 13:29:24.806  INFO 71509 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'mrpc.server-com.kongzhong.mrpc.springboot.config.RpcServerProperties' of type [com.kongzhong.mrpc.springboot.config.RpcServerProperties] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2017-07-09 13:29:24.810  INFO 71509 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'mrpc.netty-com.kongzhong.mrpc.springboot.config.NettyProperties' of type [com.kongzhong.mrpc.springboot.config.NettyProperties] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2017-07-09 13:29:24.814  INFO 71509 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'mrpc.admin-com.kongzhong.mrpc.springboot.config.AdminProperties' of type [com.kongzhong.mrpc.springboot.config.AdminProperties] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2017-07-09 13:29:24.815  INFO 71509 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'com.kongzhong.mrpc.springboot.server.RpcServerAutoConfigure' of type [com.kongzhong.mrpc.springboot.server.RpcServerAutoConfigure] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)

		    __  _______  ____  ______
		   /  |/  / __ \/ __ \/ ____/
		  / /|_/ / /_/ / /_/ / /
		 / /  / / _, _/ ____/ /___
		/_/  /_/_/ |_/_/    \____/

	　:: mrpc server :: (0.0.9-SNAPSHOT)

2017-07-09 13:29:24.893 DEBUG 71509 --- [           main] c.k.m.s.server.RpcServerAutoConfigure    : Initializing rpc server beanFactoryAware
2017-07-09 13:29:24.894  INFO 71509 --- [           main] c.kongzhong.mrpc.server.SimpleRpcServer  : RPC server connect zookeeper address: 127.0.0.1:2181
2017-07-09 13:29:25.139  INFO 71509 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2017-07-09 13:29:25.313  INFO 71509 --- [           main] c.kongzhong.mrpc.server.SimpleRpcServer  : Register => [com.kongzhong.mrpc.demo.service.BenchmarkService] - [127.0.0.1:5067]
2017-07-09 13:29:25.315  INFO 71509 --- [           main] c.kongzhong.mrpc.server.SimpleRpcServer  : Register => [com.kongzhong.mrpc.demo.service.UserService] - [127.0.0.1:5067]
2017-07-09 13:29:25.315 DEBUG 71509 --- [           main] c.kongzhong.mrpc.server.SimpleRpcServer  : RPC server backend listen destroy
2017-07-09 13:29:25.316  INFO 71509 --- [           main] c.kongzhong.mrpc.server.SimpleRpcServer  : Publish services finished, mrpc version [0.0.9A-SNAPSHOT]
```

## 3. 客户端

### 3.1 添加依赖

```xml
<!-- 引入接口定义 -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>example-api</artifactId>
    <version>0.0.1</version>
</dependency>

<!-- RPC sprint-boot starter -->
<dependency>
    <groupId>com.kongzhong.mrpc</groupId>
    <artifactId>mrpc-spring-boot-starter</artifactId>
    <version>[最新版本]</version>
</dependency>
```

### 3.2 客户端调用

#### 配置文件

```properties
logging.level.com.kongzhong.mrpc=debug
```

#### 启动类

```java
@SpringBootApplication
public class BootClientApplication {

    // 添加接口引用
    @Bean
    public Referers referers() {
        return new Referers().add(UserService.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BootClientApplication.class, args);
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

启动服务端后运行 `BootClientApplication`

```bash
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.5.2.RELEASE)

2017-07-09 13:34:30.772  INFO 74770 --- [           main] c.k.mrpc.client.BootClientApplication    : Starting BootClientApplication on biezhi.local with PID 74770 (/Users/biezhi/workspace/projects/java/mrpc/mrpc-demo/spring-boot-client/target/classes started by biezhi in /Users/biezhi/workspace/projects/java/mrpc)
2017-07-09 13:34:30.776 DEBUG 74770 --- [           main] c.k.mrpc.client.BootClientApplication    : Running with Spring Boot v1.5.2.RELEASE, Spring v4.3.7.RELEASE
2017-07-09 13:34:30.776  INFO 74770 --- [           main] c.k.mrpc.client.BootClientApplication    : No active profile set, falling back to default profiles: default
2017-07-09 13:34:30.849  INFO 74770 --- [           main] ationConfigEmbeddedWebApplicationContext : Refreshing org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@2898ac89: startup date [Sun Jul 09 13:34:30 CST 2017]; root of context hierarchy

		    __  _______  ____  ______
		   /  |/  / __ \/ __ \/ ____/
		  / /|_/ / /_/ / /_/ / /
		 / /  / / _, _/ ____/ /___
		/_/  /_/_/ |_/_/    \____/

	　:: mrpc client :: (0.0.9A-SNAPSHOT)

2017-07-09 13:34:31.884 DEBUG 74770 --- [           main] c.k.m.springboot.client.PropertiesParse  : RpcClientProperties(transport=http, appId=demo, directAddress=null, haStrategy=FAILOVER, lbStrategy=ROUND, serialize=kyro, skipBind=false, waitTimeout=10000, pingInterval=-1, failOverRetry=3, retryInterval=3000, retryCount=10)
2017-07-09 13:34:31.889 DEBUG 74770 --- [           main] c.k.m.springboot.client.PropertiesParse  : CommonProperties(registry={default={}}, custom={}, test=null, netty=null)
2017-07-09 13:34:32.005  INFO 74770 --- [           main] c.kongzhong.mrpc.client.SimpleRpcClient  : ClientConfig(super=com.kongzhong.mrpc.config.ClientConfig@d99be6d3, appId=demo, haStrategy=FAILOVER, rpcSerialize=com.kongzhong.mrpc.serialize.KyroSerialize@538613b3, lbStrategy=ROUND, transport=HTTP, skipBind=false, waitTimeout=10000, failOverRetry=3, retryInterval=3000, retryCount=10, pingInterval=-1)
2017-07-09 13:34:32.132 DEBUG 74770 --- [           main] com.kongzhong.mrpc.client.Connections    : Sync connect 127.0.0.1:5067
2017-07-09 13:34:32.373  INFO 74770 --- [           main] c.k.mrpc.transport.netty.NettyClient     : Connect [id: 0xdf3f563e, L:/127.0.0.1:62102 - R:/127.0.0.1:5067] success.
2017-07-09 13:34:32.373 DEBUG 74770 --- [ntLoopGroup-2-1] c.k.m.t.netty.SimpleClientHandler        : Channel actived: [id: 0xdf3f563e, L:/127.0.0.1:62102 - R:/127.0.0.1:5067]
2017-07-09 13:34:32.389  INFO 74770 --- [           main] c.kongzhong.mrpc.client.SimpleRpcClient  : Bind rpc service [com.kongzhong.mrpc.demo.service.UserService]
2017-07-09 13:34:32.390  INFO 74770 --- [           main] c.k.m.springboot.client.BootRpcClient    : Bind services finished
2017-07-09 13:34:32.858  INFO 74770 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat initialized with port(s): 8080 (http)
2017-07-09 13:34:32.871  INFO 74770 --- [           main] o.apache.catalina.core.StandardService   : Starting service Tomcat
2017-07-09 13:34:32.872  INFO 74770 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet Engine: Apache Tomcat/8.5.11
2017-07-09 13:34:32.963  INFO 74770 --- [ost-startStop-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2017-07-09 13:34:32.963  INFO 74770 --- [ost-startStop-1] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 2117 ms
2017-07-09 13:34:33.090  INFO 74770 --- [ost-startStop-1] o.s.b.w.servlet.ServletRegistrationBean  : Mapping servlet: 'dispatcherServlet' to [/]
2017-07-09 13:34:33.097  INFO 74770 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'characterEncodingFilter' to: [/*]
2017-07-09 13:34:33.097  INFO 74770 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'hiddenHttpMethodFilter' to: [/*]
2017-07-09 13:34:33.097  INFO 74770 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'httpPutFormContentFilter' to: [/*]
2017-07-09 13:34:33.097  INFO 74770 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'requestContextFilter' to: [/*]
2017-07-09 13:34:33.377  INFO 74770 --- [           main] s.w.s.m.m.a.RequestMappingHandlerAdapter : Looking for @ControllerAdvice: org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext@2898ac89: startup date [Sun Jul 09 13:34:30 CST 2017]; root of context hierarchy
2017-07-09 13:34:33.440  INFO 74770 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/],methods=[GET]}" onto public java.lang.String com.kongzhong.mrpc.client.IndexController.index(java.lang.String,java.util.Optional<java.lang.Integer>)
2017-07-09 13:34:33.442  INFO 74770 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/error]}" onto public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)
2017-07-09 13:34:33.442  INFO 74770 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/error],produces=[text/html]}" onto public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
2017-07-09 13:34:33.472  INFO 74770 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/webjars/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2017-07-09 13:34:33.472  INFO 74770 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2017-07-09 13:34:33.506  INFO 74770 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/**/favicon.ico] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2017-07-09 13:34:33.711  INFO 74770 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2017-07-09 13:34:33.765  INFO 74770 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8080 (http)
2017-07-09 13:34:33.771  INFO 74770 --- [           main] c.k.mrpc.client.BootClientApplication    : Started BootClientApplication in 3.521 seconds (JVM running for 4.053)
```

访问 http://127.0.0.1:8080/hello?msg=spring-boot 会得到

<img src="https://ooo.0o0.ooo/2017/07/09/5961c14f1234d.png" width="500"/>

