package com.kongzhong.mrpc.spring.parser;

import com.kongzhong.mrpc.config.ServerConfig;
import com.kongzhong.mrpc.server.RpcSpringInit;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ServerConfigBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(RpcSpringInit.class);

        String appId = element.getAttribute("appId");
        String address = element.getAttribute("address");
        String elasticIp = element.getAttribute("elasticIp");
        String transport = element.getAttribute("transport");
        String serialize = element.getAttribute("serialize");

        builder.addPropertyValue("appId", appId);
        builder.addPropertyValue("address", address);
        builder.addPropertyValue("elasticIp", elasticIp);
        builder.addPropertyValue("transport", transport);
        builder.addPropertyValue("serialize", serialize);

        return builder.getBeanDefinition();
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}