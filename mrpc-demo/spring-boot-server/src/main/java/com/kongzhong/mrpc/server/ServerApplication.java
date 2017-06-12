package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.metric.InfluxdbProperties;
import com.kongzhong.mrpc.metric.MetricInterceptor;
import com.kongzhong.mrpc.metric.MetricsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author biezhi
 *         2017/5/15
 */
@SpringBootApplication
@EnableConfigurationProperties(InfluxdbProperties.class)
public class ServerApplication {

    @Autowired
    private InfluxdbProperties influxdbProperties;

    @Bean
    public MetricInterceptor metricInterceptor() {
        MetricsClient metricsClient = new MetricsClient(influxdbProperties);
        MetricInterceptor metricInterceptor = new MetricInterceptor(metricsClient);
        return metricInterceptor;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
