package com.demo;

import com.kongzhong.mrpc.client.RpcClient;
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
    public RpcClient rpcClient() {
        RpcClient rpcClient = new RpcClient("127.0.0.1:5066");
        rpcClient.bindReferer(UserService.class);
        return rpcClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
