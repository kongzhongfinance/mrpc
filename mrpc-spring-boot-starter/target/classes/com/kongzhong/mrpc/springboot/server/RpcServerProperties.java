package com.kongzhong.mrpc.springboot.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
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
    private String transport;
    private String registry;
    private String appId;

}
