package com.kongzhong.mrpc.spring.parser;

import com.kongzhong.mrpc.config.NettyConfig;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class NettyBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(NettyConfig.class);

        String connTimeout = element.getAttribute("connTimeout");
        String backlog = element.getAttribute("backlog");
        String lowWaterMark = element.getAttribute("lowWaterMark");
        String highWaterMark = element.getAttribute("highWaterMark");

        builder.addPropertyValue("connTimeout", Integer.valueOf(connTimeout));
        builder.addPropertyValue("backlog", Integer.valueOf(backlog));
        builder.addPropertyValue("lowWaterMark", Integer.valueOf(lowWaterMark));
        builder.addPropertyValue("highWaterMark", Integer.valueOf(highWaterMark));

        return builder.getBeanDefinition();
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}