package com.kongzhong.mrpc.spring.parser;

import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.utils.StringUtils;
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

        if (StringUtils.isNotEmpty(connTimeout)) {
            builder.addPropertyValue("connTimeout", Integer.valueOf(connTimeout));
        }

        if (StringUtils.isNotEmpty(backlog)) {
            builder.addPropertyValue("backlog", Integer.valueOf(backlog));
        }

        if (StringUtils.isNotEmpty(lowWaterMark)) {
            builder.addPropertyValue("lowWaterMark", Integer.valueOf(lowWaterMark));
        }

        if (StringUtils.isNotEmpty(highWaterMark)) {
            builder.addPropertyValue("highWaterMark", Integer.valueOf(highWaterMark));
        }

        return builder.getBeanDefinition();
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}