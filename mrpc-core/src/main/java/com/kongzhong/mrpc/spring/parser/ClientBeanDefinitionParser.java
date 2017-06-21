package com.kongzhong.mrpc.spring.parser;

import com.kongzhong.mrpc.model.ClientBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ClientBeanDefinitionParser extends AbstractBeanDefinitionParser {

    public ClientBeanDefinitionParser() {
    }

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ClientBean.class);

        String id = element.getAttribute("id");
        String interfaceName = element.getAttribute("interface");

        builder.addPropertyValue("id", id);
        builder.addPropertyValue("serviceName", interfaceName);
        return builder.getBeanDefinition();
    }

    protected boolean shouldGenerateId() {
        return true;
    }
}