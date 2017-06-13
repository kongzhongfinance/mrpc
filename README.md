# mrpc (v2)

## 特性

- 调用透明
- 高性能
- 支持分布式(服务注册和发现)
- 集成Spring/SpringBoot
- 支持TCP/HTTP通讯
- 负载均衡策略
- 容错处理(FailOver/FailFast)
- 拦截器处理, 插件式扩展
- 客户端断线重连
- 动态注册/卸载服务
- 秒级监控
- 服务调用链查看

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

### 更多例子

[这里](/mrpc-demo)有更多的使用案例。