package com.kongzhong.mrpc.demo.http.registry;

import com.kongzhong.mrpc.demo.service.PayService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Created by biezhi on 06/07/2017.
 */
public class TraceApplication {

    public static void main(String[] args) throws Exception {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-client-trace-registry.xml");
        PayService payService = ctx.getBean(PayService.class);

        TimeUnit.SECONDS.sleep(2);

        String result = payService.pay("hello", new BigDecimal("20"));
        System.out.println(result);
    }
}
