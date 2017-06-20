package com.kongzhong.mrpc.springboot.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * mrpc服务端配置
 *
 * @author biezhi
 *         2017/5/13
 */
@ConfigurationProperties("mrpc.server")
@Data
public class RpcServerProperties {

    // 服务绑定ip:port
    private String address;

    // 外网弹性ip:port，不清楚不用填写
    private String elasticIp;

    // 服务端传输协议，默认tcp
    private String transport = "tcp";

    // 服务端注册中心
    private String registry;

    // 服务所属appId
    private String appId;

    // 是否是测试环境, 为 "true" 时服务进程不会挂起
    private String test;

    // 业务线程池前缀
    private String poolName = "mrpc-server";

}