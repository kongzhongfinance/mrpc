package com.kongzhong.mrpc.springboot.client;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.client.Referers;
import com.kongzhong.mrpc.client.SimpleRpcClient;
import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.interceptor.RpcClientInteceptor;
import com.kongzhong.mrpc.registry.DefaultDiscovery;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.springboot.config.CommonProperties;
import com.kongzhong.mrpc.springboot.config.RpcClientProperties;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

import static com.kongzhong.mrpc.Const.MRPC_CLIENT_DISCOVERY_PREFIX;

/**
 * Spring Boot启动器
 *
 * @author biezhi
 *         2017/4/25
 */
@Slf4j
@NoArgsConstructor
public class BootRpcClient extends SimpleRpcClient implements BeanDefinitionRegistryPostProcessor {

    private CommonProperties commonProperties;
    private RpcClientProperties rpcClientProperties;

    /**
     * 自定义服务配置
     */
    private Map<String, Map<String, String>> customServiceMap = Maps.newHashMap();

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.debug("BootRpcClient postProcessBeanFactory");

        // 加载配置
        Referers referersObject = beanFactory.getBean(Referers.class);
        super.referers = referersObject.getReferers();

        Map<String, RpcClientInteceptor> rpcClientInteceptorMap = beanFactory.getBeansOfType(RpcClientInteceptor.class);
        if (null != rpcClientInteceptorMap) {
            rpcClientInteceptorMap.values().forEach(super::addInterceptor);
        }

        ConfigurableEnvironment configurableEnvironment = beanFactory.getBean(ConfigurableEnvironment.class);

        this.rpcClientProperties = PropertiesParse.getRpcClientProperties(configurableEnvironment);
        this.commonProperties = PropertiesParse.getCommonProperties(configurableEnvironment);
        this.customServiceMap = this.commonProperties.getCustom();

        super.transport = rpcClientProperties.getTransport();
        super.appId = rpcClientProperties.getAppId();
        super.directAddress = rpcClientProperties.getDirectAddress();

        // 注册中心
        if (null != commonProperties.getRegistry() && !commonProperties.getRegistry().isEmpty()) {
            commonProperties.getRegistry().forEach((registryName, map) -> {
                ServiceDiscovery serviceDiscovery = mapToDiscovery(map);
                serviceDiscoveryMap.put(registryName, serviceDiscovery);
                beanFactory.registerSingleton(MRPC_CLIENT_DISCOVERY_PREFIX + registryName, serviceDiscovery);
                usedRegistry = true;
            });
        }

        if (!usedRegistry && StringUtils.isEmpty(rpcClientProperties.getDirectAddress())) {
            throw new SystemException("Service discovery or direct must select one.");
        }
        try {
            super.init();
            // 初始化客户端引用服务
            referers.forEach(referer -> super.initReferer(referer, beanFactory));
        } catch (Exception e) {
            log.error("RPC client init error", e);
        }
    }

    private ServiceDiscovery mapToDiscovery(Map<String, String> map) {
        String type = map.get("type");
        if (RegistryEnum.DEFAULT.getName().equals(type)) {
            ServiceDiscovery serviceDiscovery = new DefaultDiscovery();
            return serviceDiscovery;
        }
        if (RegistryEnum.ZOOKEEPER.getName().equals(type)) {
            String zkAddr = map.getOrDefault("address", "127.0.0.1:2181");
            log.info("RPC server connect zookeeper address: {}", zkAddr);
            try {
                Object zookeeperServiceDiscovery = Class.forName("com.kongzhong.mrpc.discover.ZookeeperServiceDiscovery").getConstructor(String.class).newInstance(zkAddr);
                ServiceDiscovery serviceDiscovery = (ServiceDiscovery) zookeeperServiceDiscovery;
                return serviceDiscovery;
            } catch (Exception e) {
                log.error("Setting service discovery error", e);
            }
        }
        return null;
    }

}