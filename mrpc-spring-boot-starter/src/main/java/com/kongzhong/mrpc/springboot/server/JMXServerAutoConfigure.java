package com.kongzhong.mrpc.springboot.server;

import com.kongzhong.mrpc.config.ServerConfig;
import com.kongzhong.mrpc.mbean.ServiceStatusTable;
import com.kongzhong.mrpc.springboot.config.JMXServerProperties;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.jmx.support.ConnectorServerFactoryBean;
import org.springframework.remoting.rmi.RmiRegistryFactoryBean;

/**
 * 服务端JMX监控
 * <p>
 * Created by biezhi on 01/07/2017.
 */
@Slf4j
@ConditionalOnProperty(prefix = "mrpc.server.jmx-monitor", value = "enabled", havingValue = "true")
@EnableConfigurationProperties(JMXServerProperties.class)
public class JMXServerAutoConfigure {

    @Autowired
    private JMXServerProperties jmxServerProperties;

    @Bean
    public ServerConfig serverConfig() {
        return ServerConfig.me();
    }

    @Bean
    public ServiceStatusTable serviceStatusTable() {
        return ServiceStatusTable.me();
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

        String url = StringUtils.isNotEmpty(jmxServerProperties.getUrl()) ? jmxServerProperties.getUrl() : "service:jmx:rmi://localhost/jndi/rmi://localhost:1099/mrpc/status";

        log.info("RPC server JMX url: {}", url);

        connectorServerFactoryBean.setServiceUrl(url);

        return connectorServerFactoryBean;
    }

}
