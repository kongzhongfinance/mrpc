package com.kongzhong.mrpc.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author biezhi
 *         2017/5/15
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.kongzhong.mrpc.server.service")
// metrics
//@EnableConfigurationProperties(value = {MetricsProperties.class})
public class ServerApplication {

    /*@Autowired
    private MetricsProperties metricsProperties;

    @Bean
    public MetricsClient metricsClient() {
        log.info("{}", metricsProperties);
        return new MetricsClient(metricsProperties);
    }

    @Bean
    public MetricsInterceptor metricInterceptor() {
        MetricsInterceptor metricsInterceptor = new MetricsInterceptor(metricsClient());
        return metricsInterceptor;
    }*/

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
