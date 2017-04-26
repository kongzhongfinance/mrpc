package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.*;
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
    }

    public BootRpcClient(String serverAddr) {
        this.serverAddr = serverAddr;
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

    private void registerBean(BeanDefinitionRegistry registry, String name, Class<?> beanClass) {
        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);

        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);

        abd.setScope(scopeMetadata.getScopeName());
        // 可以自动生成name
        String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, registry));

        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);

        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);

        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        log.info("setBeanFactory");
        Assert.state(beanFactory instanceof ConfigurableBeanFactory, "wrong bean factory type");
        configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
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
