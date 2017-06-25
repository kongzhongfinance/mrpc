package com.kongzhong.mrpc.spring.parser;

import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.utils.ReflectUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ClientBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ClientBean.class);

        String id = element.getAttribute("id");
        String interfaceName = element.getAttribute("interface");
        String version = element.getAttribute("version");
        String directAddress = element.getAttribute("directAddress");
        String registry = element.getAttribute("registry");

        builder.addPropertyValue("id", id);
        builder.addPropertyValue("serviceName", interfaceName);
        builder.addPropertyValue("version", version);
        builder.addPropertyValue("serviceClass", ReflectUtils.from(interfaceName));
        builder.addPropertyValue("directAddress", directAddress);
        builder.addPropertyValue("registry", registry);
        return builder.getBeanDefinition();
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}