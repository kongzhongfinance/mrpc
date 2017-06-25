package com.kongzhong.mrpc.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author biezhi
 *         2017/5/15
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.kongzhong.mrpc.server.service","com.kongzhong.mrpc.server.config"})
public class CustomServerApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "custom");
        SpringApplication.run(CustomServerApplication.class, args);
    }
}
