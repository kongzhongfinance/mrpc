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

    private String address;
    private String transport;
    private String registry;
    private String appId;

}
