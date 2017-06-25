# mrpc 

mrpc是一款简洁易用的分布式服务化治理框架。

## 特性

- 调用透明,像调用本地方法一样使用RPC服务
- 高性能
- 支持分布式,基于Zookeeper/Consul的服务注册和发现
- 完美集成Spring/SpringBoot项目
- 支持TCP/HTTP传输协议
- 多种负载均衡策略
- 集群下的容错处理(FailOver/FailFast)
- 拦截器处理, 插件式扩展
- 客户端断线重连，自动恢复
- 动态注册/卸载服务
- 秒级监控，使用statsd-influxdb-grafana打点预览
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

### 更新日志

#### 0.0.8-SNAPSHOT

1. 客户端断线自动重连
2. 允许跳过启动绑定服务
3. 允许自定义异常

#### 0.0.7-SNAPSHOT (2017/06/22)

1. 增加多注册中心配置
2. 增加单个服务更小粒度配置
3. 增加直连服务选项
4. 修复方法泛型入参/返回值
5. 客户端增加拦截器
6. 增加 `mrpc` sehema 配置项
