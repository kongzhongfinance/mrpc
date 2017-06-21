package com.kongzhong.mrpc.spring.parser;

import com.kongzhong.mrpc.config.ClientConfig;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ClientConfigBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ClientConfig.class);

        String appId = element.getAttribute("appId");
        String transport = element.getAttribute("transport");
        String serialize = element.getAttribute("serialize");

        builder.addPropertyValue("appId", appId);
        builder.addPropertyValue("transport", transport);
        builder.addPropertyValue("serialize", serialize);

        return builder.getBeanDefinition();
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}