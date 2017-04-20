package com.kongzhong.mrpc.demo.http;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author biezhi
 *         2017/4/19
 */
public class HttpServerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-server-http.xml");
        ctx.registerShutdownHook();
        ctx.start();
    }
}
