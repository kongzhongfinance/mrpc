package com.kongzhong.mrpc.demo.tcp;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.model.StatusEnum;
import com.kongzhong.mrpc.demo.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ReconnectApplication {

    public static void main(String[] args) throws Exception {

        ApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-client-reconnect.xml");
        final UserService userService = ctx.getBean(UserService.class);

        while (true) {
            userService.hello("aaa");
            TimeUnit.SECONDS.sleep(2);
        }

    }
}
