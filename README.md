# mrpc (v2)

## 特性

- 调用透明
- 高性能
- 支持分布式
- 集成Spring/SpringBoot
- 支持TCP/HTTP通讯

## HTTP协议快速入门

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
client.setTransfer("http");

UserService userService = client.getProxyBean(UserService.class);
System.out.println(userService.hello("mrpc"));
```

## 包规划

- codec: 编解码
- serialize: 序列化
- transport: 传输层
- spring: 整合spring
- server: rpc服务端
- client: rpc客户端
- ha: 高可用（负载均衡，容错，快速失败，限流）
- monitor: 指标监控
- trace: 服务调用链
- filter: 服务拦截器
- registry: 服务注册
- exception: 异常处理
