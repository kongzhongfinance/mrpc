package com.kongzhong.mrpc.demo.helloworld;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ServerApplication {

    public static void main(String[] args) {
        System.out.println("RPCServerApplication start...");
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-server.xml");
        ctx.registerShutdownHook();
        ctx.start();
    }
}
