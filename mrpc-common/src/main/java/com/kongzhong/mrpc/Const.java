package com.kongzhong.mrpc;

/**
 * mrpc常量
 *
 * @author biezhi
 * 2017/6/9
 */
public interface Const {

    String VERSION = "0.1.1-SNAPSHOT";

    String BANNER = "\n" +
            "\t\t    __  _______  ____  ______\n" +
            "\t\t   /  |/  / __ \\/ __ \\/ ____/\n" +
            "\t\t  / /|_/ / /_/ / /_/ / /     \n" +
            "\t\t / /  / / _, _/ ____/ /___   \n" +
            "\t\t/_/  /_/_/ |_/_/    \\____/   \n" +
            "                             ";


    String SERVER_BANNER = BANNER +
            "\n\t　:: mrpc server :: (" + Const.VERSION + ")\n";

    String CLIENT_BANNER = BANNER +
            "\n\t　:: mrpc client :: (" + Const.VERSION + ")\n";

    // http头信息请求id key
    String HEADER_REQUEST_ID                   = "requestId";
    // http头信息 服务接口名
    String HEADER_SERVICE_CLASS                = "serviceName";
    // http头信息 传递方法名
    String HEADER_METHOD_NAME                  = "methodName";
    // 拦截器名称前缀
    String SERVER_INTERCEPTOR_PREFIX           = "server-interceptor-";
    String CLIENT_INTERCEPTOR_PREFIX           = "client-interceptor-";
    // 测试环境变量
    String TEST_KEY                            = "mrpc.test";
    // 测试环境
    String TEST_ENV                            = "mrpc.test=true";
    // 客户端传输协议
    String TRANSPORT_CLIENT                    = "mrpc.client.transport";
    String SERVER_TRANSPORT                    = "mrpc.server.transport";
    String SERVER_ADDRESS                      = "mrpc.server.address";
    // 客户端appId
    String CLINET_APP_ID_STYLE1                = "mrpc.client.appId";
    String CLINET_APP_ID_STYLE2                = "mrpc.client.app-id";
    // 客户端序列化配置
    String SERIALIZE_CLIENT                    = "mrpc.client.serialize";
    // 客户端负载均衡配置
    String LB_STRATEGY_STYLE1_CLIENT           = "mrpc.client.lb-strategy";
    String LB_STRATEGY_STYLE2_CLIENT           = "mrpc.client.lbStrategy";
    // 客户端高可用配置
    String HA_STRATEGY_STYLE1_CLIENT           = "mrpc.client.ha-strategy";
    String HA_STRATEGY_STYLE2_CLIENT           = "mrpc.client.haStrategy";
    // 客户端直连地址配置
    String DIRECT_ADDRESS_STYLE1_CLIENT        = "mrpc.client.directAddress";
    String DIRECT_ADDRESS_STYLE2_CLIENT        = "mrpc.client.direct-address";
    // 客户端等待超时配置
    String WAIT_TIMEOUT_STYLE1_CLIENT          = "mrpc.client.wait-timeout";
    String WAIT_TIMEOUT_STYLE2_CLIENT          = "mrpc.client.waitTimeout";
    // 客户端定时ping服务端间隔
    String PING_INTERVAL_STYLE1_CLIENT         = "mrpc.client.pingInterval";
    String PING_INTERVAL_STYLE2_CLIENT         = "mrpc.client.ping-interval";
    // 客户端FailOver重试次数
    String FAILOVER_TRCRY_NUMBER_STYLE1_CLIENT = "mrpc.client.failover-retry";
    String FAILOVER_TRCRY_NUMBER_STYLE2_CLIENT = "mrpc.client.failoverRetry";
    // 重试间隔，单位/毫秒
    String RETRY_INTERVAL_STYLE1_CLIENT        = "mrpc.client.retryInterval";
    String RETRY_INTERVAL_STYLE2_CLIENT        = "mrpc.client.retry-interval";
    // 重试次数，默认10次
    String RETRY_COUNT_STYLE1_CLIENT           = "mrpc.client.retryCount";
    String RETRY_COUNT_STYLE2_CLIENT           = "mrpc.client.retry-count";
    // 跳过服务绑定
    String SKIP_BIND_SERVICE_STYLE1_CLIENT     = "mrpc.client.skipBind";
    String SKIP_BIND_SERVICE_STYLE2_CLIENT     = "mrpc.client.skip-bind";
    // netty配置前缀
    String NETTY_CONFIG_PREFIX                 = "mrpc.netty";
    // netty客户端连接超时时长，单位/毫秒
    String NETTY_CONN_STYLE1_TIMEOUT           = "mrpc.netty.connTimeout";
    String NETTY_CONN_STYLE2_TIMEOUT           = "mrpc.netty.conn-timeout";
    // netty tcp backlog
    String NETTY_BACKLOG                       = "mrpc.netty.backlog";
    // 注册中心
    String REGSITRY_KEY                        = "mrpc.registry";
    // 自定义配置
    String CUSTOM_KEY                          = "mrpc.custome";
    // 服务端注册中心bean前缀
    String MRPC_SERVER_REGISTRY_PREFIX         = "mrpc-server-registry-";
    // 客户端服务发现bean前缀
    String MRPC_CLIENT_DISCOVERY_PREFIX        = "mrpc-client-discovery-";
    String EMPTY_SERVER                        = "empty_server";
    String SERVER_HOST                         = "server_host";
    String SERVER_PORT                         = "server_port";
    String APP_ID                              = "app_id";
    String APP_NAME                            = "app_name";
    String SERVER_NAME                         = "server_name";
    String SERVER_OWNER                        = "server_owner";
    String SERVER_OWNER_EMAIL                  = "server_owner_email";
    String SERVER_EXCEPTION                    = "server_exception";

}