package com.kongzhong.mrpc.demo.tcp;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 服务端拦截器
 *
 * @author biezhi
 *         2017/4/19
 */
public class InterceptorServerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-server-inter.xml");
        ctx.registerShutdownHook();
        ctx.start();
    }
}
