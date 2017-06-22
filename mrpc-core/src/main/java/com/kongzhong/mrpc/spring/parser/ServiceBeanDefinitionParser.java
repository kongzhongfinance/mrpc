package com.kongzhong.mrpc.spring.parser;

import com.kongzhong.mrpc.model.ServiceBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ServiceBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ServiceBean.class);

        String serviceName = element.getAttribute("interface");
        String ref = element.getAttribute("ref");
        String address = element.getAttribute("address");
        String elasticIp = element.getAttribute("elasticIp");
        String registry = element.getAttribute("registry");

        builder.addPropertyValue("serviceName", serviceName);
        builder.addPropertyValue("beanName", ref);
        builder.addPropertyValue("address", address);
        builder.addPropertyValue("elasticIp", elasticIp);
        builder.addPropertyValue("registry", registry);

        return builder.getBeanDefinition();
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}