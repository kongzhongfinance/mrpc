package com.kongzhong.mrpc.demo.registry;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author biezhi
 *         2017/4/19
 */
public class LoadlServer1 {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-local-server-1.xml");
        ctx.registerShutdownHook();
        ctx.start();
    }
}
