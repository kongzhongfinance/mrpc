package com.kongzhong.mrpc.demo.tcp.registry;

import com.kongzhong.mrpc.demo.service.UserService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author biezhi
 *         2017/4/19
 */
public class DiscoveryClientApplication {
    public static void main(String[] args) throws Exception {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-client-registry.xml");
        UserService userService = ctx.getBean(UserService.class);
        int index = 1;
        while (true) {
            System.out.println(userService.add(10, index++));
            Thread.sleep(3000);
        }
    }
}
