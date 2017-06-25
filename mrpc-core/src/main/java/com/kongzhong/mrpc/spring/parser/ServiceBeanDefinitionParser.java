package com.kongzhong.mrpc.spring.parser;

import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.utils.StringUtils;
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
        String appId = element.getAttribute("appId");
        String version = element.getAttribute("version");
        String beanName = element.getAttribute("ref");
        String address = element.getAttribute("address");
        String elasticIp = element.getAttribute("elasticIp");
        String registry = element.getAttribute("registry");

        builder.addPropertyValue("serviceName", serviceName);
        if (StringUtils.isNotEmpty(appId)) {
            builder.addPropertyValue("appId", appId);
        }
        if (StringUtils.isNotEmpty(beanName)) {
            builder.addPropertyValue("beanName", beanName);
        }
        if (StringUtils.isNotEmpty(address)) {
            builder.addPropertyValue("address", address);
        }
        if (StringUtils.isNotEmpty(elasticIp)) {
            builder.addPropertyValue("elasticIp", elasticIp);
        }
        if (StringUtils.isNotEmpty(registry)) {
            builder.addPropertyValue("registry", registry);
        }
        if (StringUtils.isNotEmpty(beanName)) {
            builder.addPropertyReference("bean", beanName);
        }
        if (StringUtils.isNotEmpty(version)) {
            builder.addPropertyValue("version", version);
        }
        return builder.getBeanDefinition();
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}