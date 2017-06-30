package com.kongzhong.mrpc.springboot.config;

import com.kongzhong.mrpc.config.ServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.jmx.support.ConnectorServerFactoryBean;
import org.springframework.remoting.rmi.RmiRegistryFactoryBean;

/**
 * 服务端JMX监控
 * <p>
 * Created by biezhi on 01/07/2017.
 */
@ConditionalOnProperty(prefix = "mrpc.server.jmx-monitor", value = "enabled", havingValue = "true")
public class JMXServerConfig {

    @Bean
    public ServerConfig serverConfig() {
        return ServerConfig.me();
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
