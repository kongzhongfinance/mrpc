package com.demo;

import com.kongzhong.mrpc.client.BootRpcClient;
import com.kongzhong.mrpc.demo.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author biezhi
 *         2017/4/24
 */
@SpringBootApplication
public class Application {

    @Bean
    public BootRpcClient bootRpcClient() {
        BootRpcClient bootRpcClient = new BootRpcClient("127.0.0.1:5006");
        bootRpcClient.bindReferer(UserService.class);
        return bootRpcClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
