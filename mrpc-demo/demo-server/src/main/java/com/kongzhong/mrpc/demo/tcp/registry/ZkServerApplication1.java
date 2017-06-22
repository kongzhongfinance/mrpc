package com.kongzhong.mrpc.demo.tcp.registry;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ZkServerApplication1 {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-server-1.xml");
        ctx.registerShutdownHook();
        ctx.start();
    }
}
