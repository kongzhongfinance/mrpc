package com.kongzhong.mrpc.spring;

import com.kongzhong.mrpc.spring.parser.ClientBeanDefinitionParser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class RpcNameSpaceHandler extends NamespaceHandlerSupport {

    public RpcNameSpaceHandler() {
    }

    @Override
    public void init() {
        this.registerBeanDefinitionParser("referer", new ClientBeanDefinitionParser());
    }

    public BeanDefinition parse(Element element, ParserContext parserContext) {
//        parserContext.getRegistry().registerBeanDefinition(ClientRpcContext.class.getName(),
//                BeanDefinitionBuilder.rootBeanDefinition(ClientRpcContext.class).getBeanDefinition());
        return super.parse(element, parserContext);
    }

}