package com.kongzhong.demo.tcp;

import com.kongzhong.mrpc.demo.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ClientNettyConfigApplication {

    public static void main(String[] args) throws Exception {

        ApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-client-netty.xml");
        final UserService userService = ctx.getBean(UserService.class);
        int pos = 1;
        while (pos < 10_0000) {
            System.out.println(userService.add(10, pos++));
            TimeUnit.SECONDS.sleep(2);
        }

    }
}
