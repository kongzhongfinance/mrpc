package com.kongzhong.mrpc.config;

import lombok.Data;
import lombok.ToString;

/**
 * 服务端配置
 *
 * @author biezhi
 *         20/06/2017
 */
@Data
@ToString
public class RegistryConfig {

    private String type;
    private String address;
    private int connTimeout;

}
