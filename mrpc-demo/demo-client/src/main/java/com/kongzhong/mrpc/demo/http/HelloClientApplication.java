package com.kongzhong.mrpc.demo.http;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.model.Person;
import com.kongzhong.mrpc.demo.service.UserService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author biezhi
 *         2017/4/19
 */
public class HelloClientApplication {

    public static void main(String[] args) throws InterruptedException {
        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setDirectAddress("127.0.0.1:5066");
        rpcClient.setTransport("http");

        UserService userService = rpcClient.getProxyReferer(UserService.class);

        Thread.sleep(1000);

        while (true) {
            System.out.println(userService.hello("hello world http."));
            Thread.sleep(3000);
        }

    }
}
