package com.kongzhong.mrpc.model;

/**
 * mrpc常量
 *
 * @author biezhi
 *         2017/6/9
 */
public interface Const {

    // http头信息请求id key
    String HEADER_REQUEST_ID = "requestId";

    // 拦截器名称前缀
    String INTERCEPTOR_NAME_PREFIX = "mrpc:server:interceptor:";

    String TEST_KEY = "mrpc.test";

    // 测试环境
    String TEST_ENV = "mrpc.test=true";

    String THREAD_POOL_NAME = "mrpc-server";

    // 客户端传输协议
    String TRANSPORT_CLIENT = "mrpc.client.transport";

    // 客户端appId
    String APP_ID_CLIENT = "mrpc.client.appId";

    // 客户端注册中心
    String REGSITRY_CLIENT = "mrpc.client.registry";

    // 服务端zookeeper地址
    String ZK_SERVER_ADDRESS = "mrpc.server.zookeeper.address";

    // 客户端zookeeper地址
    String ZK_CLIENT_ADDRESS = "mrpc.client.zookeeper.address";

    String REGSITRY_INTERFACE = "com.kongzhong.mrpc.registry.ServiceRegistry";

}
