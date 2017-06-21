package com.kongzhong.mrpc.config;

import lombok.Data;
import lombok.ToString;

/**
 * 客户端配置
 *
 * @author biezhi
 *         20/06/2017
 */
@Data
@ToString(callSuper = true)
public class ClientConfig {

    private String directAddress;

}
