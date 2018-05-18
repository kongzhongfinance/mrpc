package com.kongzhong.mrpc.springboot.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by biezhi on 01/07/2017.
 */
@ConfigurationProperties("mrpc.admin")
@Data
@ToString
public class AdminProperties {

    private boolean enabled;
    private String url;
    private String username;
    private String password;
    private int period = 10_000;

}
