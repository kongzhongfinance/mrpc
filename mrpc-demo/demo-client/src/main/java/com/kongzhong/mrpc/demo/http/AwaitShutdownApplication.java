package com.kongzhong.mrpc.demo.http;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.service.UserService;

/**
 * @author biezhi
 * 2017/4/19
 */
public class AwaitShutdownApplication {

    public static void main(String[] args) throws InterruptedException {
        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setDirectAddress("127.0.0.1:5066");
        rpcClient.setTransport("http");

        UserService userService = rpcClient.getProxyReferer(UserService.class);
        String      result      = userService.testTimeout(8);
        System.out.println(result);

        rpcClient.shutdown();
    }
}
