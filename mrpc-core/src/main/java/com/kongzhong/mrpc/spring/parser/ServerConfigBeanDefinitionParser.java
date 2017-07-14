package com.kongzhong.mrpc.spring.parser;

import com.kongzhong.mrpc.server.RpcSpringServer;
import com.kongzhong.mrpc.utils.StringUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ServerConfigBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(RpcSpringServer.class);

        String appId        = element.getAttribute("appId");
        String address      = element.getAttribute("address");
        String weight       = element.getAttribute("weight");
        String elasticIp    = element.getAttribute("elasticIp");
        String transport    = element.getAttribute("transport");
        String serialize    = element.getAttribute("serialize");
        String interceptors = element.getAttribute("interceptors");
        String test         = element.getAttribute("test");

        builder.addPropertyValue("appId", appId);
        builder.addPropertyValue("address", address);
        if (StringUtils.isNotEmpty(weight)) {
            builder.addPropertyValue("weight", Integer.parseInt(weight));
        }
        builder.addPropertyValue("elasticIp", elasticIp);
        builder.addPropertyValue("transport", transport);
        builder.addPropertyValue("serialize", serialize);
        builder.addPropertyValue("interceptors", interceptors);
        builder.addPropertyValue("test", test);

        return builder.getBeanDefinition();
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}