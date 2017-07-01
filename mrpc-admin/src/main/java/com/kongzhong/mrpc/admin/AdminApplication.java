package com.kongzhong.mrpc.admin;

import com.kongzhong.mrpc.admin.config.RpcAdminProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author biezhi
 *         2017/5/14
 */
@SpringBootApplication
@EnableConfigurationProperties(RpcAdminProperties.class)
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

}
