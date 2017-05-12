package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * Spring Boot启动器
 *
 * @author biezhi
 *         2017/4/25
 */
@Slf4j
public class BootRpcClient extends SimpleRpcClient implements BeanFactoryAware, BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();
    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

    private ConfigurableBeanFactory configurableBeanFactory;

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
    }

    @Override
    public void setEnvironment(Environment environment) {

        log.debug("Set Environment");

        this.transport = environment.getProperty("mrpc.transport", "tcp");

        // 注册中心
        String registry = environment.getProperty("mrpc.registry", RegistryEnum.DEFAULT.getName());

        if (RegistryEnum.ZOOKEEPER.getName().equals(registry)) {
            String zkAddr = environment.getProperty("mrpc.zk.addr", "127.0.0.1:2181");
            log.info("zk address: {}", zkAddr);
            String interfaceName = "com.kongzhong.mrpc.registry.ServiceDiscovery";
            try {
                Object zookeeperServiceDiscovery = Class.forName("com.kongzhong.mrpc.discover.ZookeeperServiceDiscovery").getConstructor(String.class).newInstance(zkAddr);
                ServiceDiscovery serviceDiscovery = (ServiceDiscovery) zookeeperServiceDiscovery;
                this.setServiceDiscovery(serviceDiscovery);
                configurableBeanFactory.registerSingleton(interfaceName, serviceDiscovery);
            } catch (Exception e) {
                log.error("", e);
            }
        }

        referers.forEach(clazz -> {
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
