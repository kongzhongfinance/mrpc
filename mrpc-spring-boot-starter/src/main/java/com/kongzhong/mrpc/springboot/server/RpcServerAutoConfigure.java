package com.kongzhong.mrpc.springboot.server;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.config.AdminConfig;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.server.SimpleRpcServer;
import com.kongzhong.mrpc.springboot.config.AdminProperties;
import com.kongzhong.mrpc.springboot.config.CommonProperties;
import com.kongzhong.mrpc.springboot.config.NettyProperties;
import com.kongzhong.mrpc.springboot.config.RpcServerProperties;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;

import java.util.Map;

import static com.kongzhong.mrpc.Const.MRPC_SERVER_REGISTRY_PREFIX;

/**
 * RPC服务端自动配置
 *
 * @author biezhi
 *         2017/5/13
 */
@Conditional(ServerEnvironmentCondition.class)
@EnableConfigurationProperties({CommonProperties.class, RpcServerProperties.class, NettyProperties.class, AdminProperties.class})
@Slf4j
@ToString(callSuper = true, exclude = {"commonProperties", "rpcServerProperties", "nettyProperties", "configurableBeanFactory", "customServiceMap"})
public class RpcServerAutoConfigure extends SimpleRpcServer {

    @Autowired
    private CommonProperties commonProperties;

    @Autowired
    private RpcServerProperties rpcServerProperties;

    @Autowired
    private NettyProperties nettyProperties;

    @Autowired
    private AdminProperties adminProperties;

    @Autowired
    private ConfigurableBeanFactory configurableBeanFactory;

    /**
     * 自定义服务配置
     */
    private Map<String, Map<String, String>> customServiceMap = Maps.newHashMap();

    @Bean
    public ServiceBeanProcessor initBean() {
        System.out.println(Const.SERVER_BANNER);
        return new ServiceBeanProcessor(rpcMapping);
    }

    @Bean
    @ConditionalOnBean(ServiceBeanProcessor.class)
    public BeanFactoryAware beanFactoryAware() {
        return (beanFactory) -> {
            log.debug("Initializing rpc server beanFactoryAware ");
            // 注册中心
            if (null != commonProperties.getRegistry() && !commonProperties.getRegistry().isEmpty()) {
                commonProperties.getRegistry().forEach((registryName, map) -> {
                    ServiceRegistry serviceRegistry = super.mapToRegistry(map);
                    serviceRegistryMap.put(registryName, serviceRegistry);
                    configurableBeanFactory.registerSingleton(MRPC_SERVER_REGISTRY_PREFIX + registryName, serviceRegistry);
                    super.usedRegistry = true;
                });
            }
            if (null != commonProperties.getCustom()) {
                customServiceMap = commonProperties.getCustom();
            }

            System.out.println();

            log.debug(commonProperties.toString());
            log.debug(rpcServerProperties.toString() + "\n");

            super.appId = rpcServerProperties.getAppId();
            super.address = rpcServerProperties.getAddress();
            super.weight = rpcServerProperties.getWeight();
            super.elasticIp = rpcServerProperties.getElasticIp();
            super.poolName = rpcServerProperties.getPoolName();

            // netty参数配置
            super.nettyConfig = new NettyConfig();
            super.adminConfig = new AdminConfig();

            BeanUtils.copyProperties(nettyProperties, super.nettyConfig);
            BeanUtils.copyProperties(adminProperties, super.adminConfig);

            super.test = StringUtils.isNotEmpty(commonProperties.getTest()) ? commonProperties.getTest() : rpcServerProperties.getTest();

            super.transport = rpcServerProperties.getTransport();
            super.serialize = rpcServerProperties.getSerialize();

            configurableBeanFactory.registerSingleton("rpcMapping", rpcMapping);
        };
    }

    @Bean
    @Order(-1)
    public CommandLineRunner rpcDaemon() {
        return args -> super.startServer();
    }

    /**
     * 获取服务暴露的地址 ip:port
     *
     * @param serviceBean
     * @return
     */
    @Override
    public String getBindAddress(ServiceBean serviceBean) {
        String              address = super.getBindAddress(serviceBean);
        Map<String, String> custom  = customServiceMap.get(serviceBean.getBeanName());
        if (null != custom && custom.containsKey("address")) {
            address = custom.get("address");
        }
        return address;
    }

    @Override
    public String getRegisterElasticIp(ServiceBean serviceBean) {
        String              elasticIp = super.getRegisterElasticIp(serviceBean);
        Map<String, String> custom    = customServiceMap.get(serviceBean.getBeanName());
        if (null != custom) {
            if (custom.containsKey("elasticIp")) {
                elasticIp = custom.get("elasticIp");
            }
            if (custom.containsKey("elastic-ip")) {
                elasticIp = custom.get("elastic-ip");
            }
        }
        return elasticIp;
    }

    /**
     * 获取服务使用的注册中心
     *
     * @param serviceBean
     * @return
     */
    @Override
    public ServiceRegistry getRegistry(ServiceBean serviceBean) {
        ServiceRegistry     serviceRegistry = super.getRegistry(serviceBean);
        Map<String, String> custom          = customServiceMap.get(serviceBean.getBeanName());
        if (null != custom && custom.containsKey("registry")) {
            String registryName = custom.get("registry");
            return serviceRegistryMap.get(registryName);
        }
        return serviceRegistry;
    }

}