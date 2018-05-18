package com.kongzhong.mrpc.admin.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by biezhi on 01/07/2017.
 */
@Data
@ToString
@ConfigurationProperties("mrpc.admin")
public class RpcAdminProperties {

    private String version;
    private String user;
    private String pass;

}
