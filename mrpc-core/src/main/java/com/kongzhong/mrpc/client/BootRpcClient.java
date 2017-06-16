package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.model.Const;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * Spring Boot启动器
 *
 * @author biezhi
 *         2017/4/25
 */
@Slf4j
public class BootRpcClient extends SimpleRpcClient implements BeanFactoryAware, BeanDefinitionRegistryPostProcessor {

    private ConfigurableBeanFactory configurableBeanFactory;

    private Referers referersObj;

    public BootRpcClient() {
        super();
    }

    public BootRpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        log.info("setBeanFactory");
        Assert.state(beanFactory instanceof ConfigurableBeanFactory, "wrong bean factory type");
        configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
        this.referersObj = beanFactory.getBean(Referers.class);

        Environment environment = beanFactory.getBean(Environment.class);

        this.transport = environment.getProperty(Const.TRANSPORT_CLIENT, "tcp");
        this.appId = environment.getProperty(Const.APP_ID_CLIENT, "default");

        // 注册中心
        String registry = environment.getProperty(Const.REGSITRY_CLIENT, RegistryEnum.DEFAULT.getName());

        if (RegistryEnum.ZOOKEEPER.getName().equals(registry)) {
            String zkAddr = environment.getProperty(Const.ZK_CLIENT_ADDRESS, "127.0.0.1:2181");
            log.info("mrpc client connect zookeeper address: {}", zkAddr);
            String interfaceName = "com.kongzhong.mrpc.registry.ServiceDiscovery";
            try {
                Object zookeeperServiceDiscovery = Class.forName("com.kongzhong.mrpc.discover.ZookeeperServiceDiscovery").getConstructor(String.class).newInstance(zkAddr);
                ServiceDiscovery serviceDiscovery = (ServiceDiscovery) zookeeperServiceDiscovery;
                this.setServiceDiscovery(serviceDiscovery);
                configurableBeanFactory.registerSingleton(interfaceName, serviceDiscovery);
            } catch (Exception e) {
                log.error("Setting service discovery error", e);
            }
        }

        referersObj.getReferers().forEach(clazz -> {
            String interfaceName = clazz.getName();
            try {
                Object object = getProxyBean(clazz);
                configurableBeanFactory.registerSingleton(interfaceName, object);
                log.info("Bind rpc service [{}]", interfaceName);
            } catch (Exception e) {
                log.warn("Not found rpc service [{}] component!", interfaceName, e);
            }
        });

    }

}