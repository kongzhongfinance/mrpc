package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author biezhi
 *         2017/5/15
 */
@SpringBootApplication
public class BootClientApplication {

    @Bean
    public Referers referers() {
        return new Referers().add(UserService.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BootClientApplication.class, args);
    }
}
