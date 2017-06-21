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
@ToString(callSuper = true)
public class ServerConfig extends GlobalConfig {

    private String address;

}
