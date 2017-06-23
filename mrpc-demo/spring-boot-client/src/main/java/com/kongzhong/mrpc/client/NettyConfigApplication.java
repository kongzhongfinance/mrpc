package com.kongzhong.mrpc.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author biezhi
 *         2017/5/15
 */
@SpringBootApplication
public class NettyConfigApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "netty-config");
        SpringApplication.run(NettyConfigApplication.class, args);
    }
}
