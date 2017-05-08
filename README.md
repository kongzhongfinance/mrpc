# mrpc (v2)

## 特性

- 调用透明
- 高性能
- 支持分布式(服务注册和发现)
- 集成Spring/SpringBoot
- 支持TCP/HTTP通讯
- 负载均衡策略
- 容错处理(FailOver/FailFast)
- 拦截器处理
- 客户端断线重连
- 动态注册/卸载服务
- 秒级监控

## 快速入门

### 创建服务端

```java
public interface UserService {
    String hello(String name);
}

// 服务实现
@RpcService
public class UserServiceImpl implements UserService{
    
    @Override
    public String sayHello(String name){
        return "Hello " + name;
    }
    
}
```

### 客户端调用

```java
RpcClient client = new RpcClient("127.0.0.1:5066");
// http协议，默认情况走TCP
client.setTransfer("http");

UserService userService = client.getProxyBean(UserService.class);
System.out.println(userService.hello("mrpc"));
```

### 更多例子

[这里](/mrpc-demo)有更多的使用案例。