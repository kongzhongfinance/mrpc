package com.kongzhong.mrpc.demo.tcp;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author biezhi
 *         2017/4/19
 */
public class MetricsServerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-server-metrics.xml");
        ctx.registerShutdownHook();
        ctx.start();
    }
}
