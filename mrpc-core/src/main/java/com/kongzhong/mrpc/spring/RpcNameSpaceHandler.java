package com.kongzhong.mrpc.spring;

import com.kongzhong.mrpc.spring.parser.*;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class RpcNameSpaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        this.registerBeanDefinitionParser("referer", new ClientBeanDefinitionParser());
        this.registerBeanDefinitionParser("service", new ServiceBeanDefinitionParser());
        this.registerBeanDefinitionParser("registry", new RegistryBeanDefinitionParser());
        this.registerBeanDefinitionParser("netty", new NettyBeanDefinitionParser());
        this.registerBeanDefinitionParser("clientConfig", new ClientConfigBeanDefinitionParser());
        this.registerBeanDefinitionParser("serverConfig", new ServerConfigBeanDefinitionParser());
    }

}