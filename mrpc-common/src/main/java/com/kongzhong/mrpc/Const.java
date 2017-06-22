package com.kongzhong.mrpc;

/**
 * mrpc常量
 *
 * @author biezhi
 *         2017/6/9
 */
public interface Const {

    // http头信息请求id key
    String HEADER_REQUEST_ID = "requestId";

    // http头信息 服务接口名
    String HEADER_SERVICE_CLASS = "serviceName";

    // http头信息 传递方法名
    String HEADER_METHOD_NAME = "methodName";

    // 拦截器名称前缀
    String INTERCEPTOR_NAME_PREFIX = "mrpc:server:interceptor:";

    // 测试环境变量
    String TEST_KEY = "mrpc.test";

    // 测试环境
    String TEST_ENV = "mrpc.test=true";

    // 客户端传输协议
    String TRANSPORT_CLIENT = "mrpc.client.transport";

    // 客户端appId
    String APP_ID_CLIENT = "mrpc.client.appId";

    // 客户端序列化配置
    String SERIALIZE_CLIENT = "mrpc.client.serialize";

    // 客户端负载均衡配置
    String LB_STRATEGY_S1_CLIENT = "mrpc.client.lb-strategy";
    String LB_STRATEGY_S2_CLIENT = "mrpc.client.lbStrategy";

    // 客户端高可用配置
    String HA_STRATEGY_S1_CLIENT = "mrpc.client.ha-strategy";
    String HA_STRATEGY_S2_CLIENT = "mrpc.client.haStrategy";

    // 客户端直连地址配置
    String DIRECT_ADDRESS_S1_CLIENT = "mrpc.client.directAdress";
    String DIRECT_ADDRESS_S2_CLIENT = "mrpc.client.direct-address";

    // 客户端等待超时配置
    String WAIT_TIMEOUT_S1_CLIENT = "mrpc.client.wait-timeout";
    String WAIT_TIMEOUT_S2_CLIENT = "mrpc.client.waitTimeout";

    // 客户端重试次数
    String TRCRY_NUMBER_S1_CLIENT = "mrpc.client.retry-number";
    String TRCRY_NUMBER_S2_CLIENT = "mrpc.client.retryNumber";

    // 注册中心
    String REGSITRY_KEY = "mrpc.registry";

    // 自定义配置
    String CUSTOM_KEY = "mrpc.custome";

    // 服务注册接口名
    String REGSITRY_INTERFACE = "com.kongzhong.mrpc.registry.ServiceRegistry";

    // 服务发现接口名
    String DISCOVERY_INTERFACE = "com.kongzhong.mrpc.registry.ServiceDiscovery";

    // 服务端注册中心bean前缀
    String MRPC_SERVER_REGISTRY_PREFIX = "mrpc-server-registry-";

    // 客户端服务发现bean前缀
    String MRPC_CLIENT_DISCOVERY_PREFIX = "mrpc-client-discovery-";
}
