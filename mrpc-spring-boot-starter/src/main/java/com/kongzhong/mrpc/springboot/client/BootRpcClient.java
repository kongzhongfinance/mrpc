package com.kongzhong.mrpc.springboot.client;

import com.kongzhong.mrpc.client.Referers;
import com.kongzhong.mrpc.client.SimpleRpcClient;
import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.interceptor.RpcClientInterceptor;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.registry.DefaultDiscovery;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.springboot.config.CommonProperties;
import com.kongzhong.mrpc.springboot.config.RpcClientProperties;
import com.kongzhong.mrpc.utils.CollectionUtils;
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

    /**
     * 自定义引用配置
     */
    private Map<String, Map<String, String>> customServiceMap;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        // 解析客户端配置
        ConfigurableEnvironment configurableEnvironment = beanFactory.getBean(ConfigurableEnvironment.class);

        RpcClientProperties clientConfig = PropertiesParse.getRpcClientProperties(configurableEnvironment);
        CommonProperties commonProperties = PropertiesParse.getCommonProperties(configurableEnvironment);

        if (clientConfig.getSkipBind()) {
            log.info("RPC client skip bind service.");
            return;
        }

        // 读取Bean工厂的引用对象
        Referers referersObject = beanFactory.getBean(Referers.class);
        if (null == referersObject) {
            return;
        }

        super.clientBeans = referersObject.getReferers();

        // 客户端拦截器
        Map<String, RpcClientInterceptor> rpcClientInteceptorMap = beanFactory.getBeansOfType(RpcClientInterceptor.class);
        if (null != rpcClientInteceptorMap) {
            rpcClientInteceptorMap.values().forEach(super::addInterceptor);
        }

        this.customServiceMap = commonProperties.getCustom();
        super.nettyConfig = commonProperties.getNetty();

        super.appId = clientConfig.getAppId();
        super.transport = clientConfig.getTransport();
        super.serialize = clientConfig.getSerialize();
        super.directAddress = clientConfig.getDirectAddress();
        super.failOverRetry = clientConfig.getFailOverRetry();
        super.retryCount = clientConfig.getRetryCount();
        super.retryInterval = clientConfig.getRetryInterval();
        super.waitTimeout = clientConfig.getWaitTimeout();
        super.pingInterval = clientConfig.getPingInterval();

        // 注册中心
        if (CollectionUtils.isNotEmpty(commonProperties.getRegistry())) {
            commonProperties.getRegistry().forEach((registryName, map) -> {
                ServiceDiscovery serviceDiscovery = mapToDiscovery(map);
                serviceDiscoveryMap.put(registryName, serviceDiscovery);
                beanFactory.registerSingleton(MRPC_CLIENT_DISCOVERY_PREFIX + registryName, serviceDiscovery);
            });
        }

        try {
            super.init();
            // 初始化客户端引用服务
            clientBeans.forEach(referer -> super.initReferer(referer, beanFactory));

            super.directConnect();

            log.info("Bind services finished");

        } catch (Exception e) {
            log.error("RPC client init error", e);
        }
    }

    @Override
    protected boolean usedRegistry(ClientBean clientBean) {
        boolean usedRegistry = super.usedRegistry(clientBean);
        if (CollectionUtils.isNotEmpty(customServiceMap) && customServiceMap.containsKey(clientBean.getId())) {
            Map<String, String> customConfig = customServiceMap.get(clientBean.getId());
            if (customConfig.containsKey("registry")) {
                return StringUtils.isNotEmpty(customConfig.get("registry"));
            }
        }
        return usedRegistry;
    }

    @Override
    protected String getDirectAddress(ClientBean clientBean) {
        String directAddress = super.getDirectAddress(clientBean);
        if (CollectionUtils.isNotEmpty(customServiceMap) && customServiceMap.containsKey(clientBean.getId())) {
            Map<String, String> customConfig = customServiceMap.get(clientBean.getId());
            if (customConfig.containsKey("directAddress")) {
                return customConfig.get("directAddress");
            }
            if (customConfig.containsKey("direct-address")) {
                return customConfig.get("direct-address");
            }
        }
        return directAddress;
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