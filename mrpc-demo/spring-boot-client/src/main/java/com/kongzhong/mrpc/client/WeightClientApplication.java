package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 加权轮询客户端
 *
 * @author biezhi
 *         2017/5/15
 */
@SpringBootApplication
public class WeightClientApplication {

    @Bean
    public Referers referers() {
        return new Referers().add(UserService.class);
    }

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "weight");
        SpringApplication.run(WeightClientApplication.class, args);
    }
}
