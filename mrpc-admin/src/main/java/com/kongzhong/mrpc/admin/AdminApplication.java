package com.kongzhong.mrpc.admin;

import com.kongzhong.mrpc.admin.config.RpcAdminProperties;
import com.kongzhong.mrpc.admin.repository.NodeRepository;
import com.kongzhong.mrpc.admin.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author biezhi
 *         2017/5/14
 */
@SpringBootApplication
@EnableConfigurationProperties(RpcAdminProperties.class)
public class AdminApplication {

    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private NodeRepository nodeRepository;

    @Bean
    public CommandLineRunner commandLineRunner(){
        return (args) -> {
            serviceRepository.deleteAll();
            nodeRepository.deleteAll();
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

}
