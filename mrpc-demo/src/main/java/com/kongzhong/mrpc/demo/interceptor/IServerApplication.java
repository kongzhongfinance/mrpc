package com.kongzhong.mrpc.demo.interceptor;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author biezhi
 *         2017/4/19
 */
public class IServerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-server-inter.xml");
        ctx.registerShutdownHook();
        ctx.start();
    }
}
