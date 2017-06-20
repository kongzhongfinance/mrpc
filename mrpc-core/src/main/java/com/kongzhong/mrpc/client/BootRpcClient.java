package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import static com.kongzhong.mrpc.Const.DISCOVERY_INTERFACE;

/**
 * Spring Boot启动器
 *
 * @author biezhi
 *         2017/4/25
 */
@Slf4j
public class BootRpcClient extends SimpleRpcClient implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private Referers referersObj;
    private Environment environment;

    public BootRpcClient() {
        super();
    }

    public BootRpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.debug("BootRpcClient postProcessBeanFactory");
        // 加载配置
        this.referersObj = beanFactory.getBean(Referers.class);

        this.transport = environment.getProperty(Const.TRANSPORT_CLIENT, "tcp");
        this.appId = environment.getProperty(Const.APP_ID_CLIENT, "default");
        this.directUrl = environment.getProperty(Const.CLIENT_DIRECT_URL, "");

        if (StringUtils.isEmpty(directUrl)) {
            // 注册中心
            String registry = environment.getProperty(Const.REGSITRY_CLIENT, RegistryEnum.DEFAULT.getName());

            if (RegistryEnum.ZOOKEEPER.getName().equals(registry)) {
                String zkAddr = environment.getProperty(Const.ZK_CLIENT_ADDRESS, "127.0.0.1:2181");
                log.info("RPC client connect zookeeper address: {}", zkAddr);
                try {
                    Object zookeeperServiceDiscovery = Class.forName("com.kongzhong.mrpc.discover.ZookeeperServiceDiscovery").getConstructor(String.class).newInstance(zkAddr);
                    ServiceDiscovery serviceDiscovery = (ServiceDiscovery) zookeeperServiceDiscovery;
                    this.setServiceDiscovery(serviceDiscovery);
                    beanFactory.registerSingleton(DISCOVERY_INTERFACE, serviceDiscovery);
                } catch (Exception e) {
                    log.error("Setting service discovery error", e);
                }
            }
        }

        referersObj.getReferers().forEach(clazz -> {
            String interfaceName = clazz.getName();
            try {
                Object object = super.getProxyBean(clazz);
                beanFactory.registerSingleton(interfaceName, object);
                log.info("Bind rpc service [{}]", interfaceName);
            } catch (Exception e) {
                log.warn("Not found rpc service [{}] component!", interfaceName, e);
            }
        });

    }
}