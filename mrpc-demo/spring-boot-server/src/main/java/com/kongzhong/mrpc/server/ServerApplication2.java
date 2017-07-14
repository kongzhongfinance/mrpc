package com.kongzhong.mrpc.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author biezhi
 *         2017/5/15
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.kongzhong.mrpc.server.service")
public class ServerApplication2 {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "s2");
        SpringApplication.run(ServerApplication2.class, args);
    }
}
