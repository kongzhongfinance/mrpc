package com.kongzhong.mrpc.springboot.config;

import com.kongzhong.mrpc.config.ClientConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.jmx.support.ConnectorServerFactoryBean;
import org.springframework.remoting.rmi.RmiRegistryFactoryBean;

/**
 * 客户端JMX监控
 * <p>
 * Created by biezhi on 01/07/2017.
 */
@ConditionalOnProperty(prefix = "mrpc.client.jmx-monitor", value = "enabled", havingValue = "true")
public class JMXClientConfig {

    @Bean
    public ClientConfig clientConfig() {
        return ClientConfig.me();
    }

    @Bean
    public AnnotationMBeanExporter mbeanExporter() {
        return new AnnotationMBeanExporter();
    }

    @Bean
    public RmiRegistryFactoryBean rmiRegistry() {
        RmiRegistryFactoryBean registryFactoryBean = new RmiRegistryFactoryBean();
        registryFactoryBean.setAlwaysCreate(true);
        return registryFactoryBean;
    }

    @Bean
    public ConnectorServerFactoryBean connectorServerBean() {
        ConnectorServerFactoryBean connectorServerFactoryBean = new ConnectorServerFactoryBean();
        connectorServerFactoryBean.setServiceUrl("service:jmx:rmi://localhost/jndi/rmi://localhost:1099/mrpc/status");
        return connectorServerFactoryBean;
    }

}
