package com.kongzhong.mrpc.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author biezhi
 *         2017/5/15
 */
@SpringBootApplication
public class ServerApplication implements CommandLineRunner {

    @Override
    public void run(String... strings) throws Exception {
        Thread.currentThread().join();
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
