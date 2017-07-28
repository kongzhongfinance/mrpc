package com.kongzhong.mrpc.demo.tcp;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.service.UserService;

import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 *         2017/4/19
 */
public class TimeoutClientApplication {

    public static void main(String[] args) throws Exception {

        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setDirectAddress("127.0.0.1:5066");
        rpcClient.setWaitTimeout(1000);
//        rpcClient.setHaStrategy("failfast");

        final UserService userService = rpcClient.getProxyReferer(UserService.class);
        TimeUnit.SECONDS.sleep(2);

        try {
            userService.testTimeout(3_000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("停止");
        TimeUnit.SECONDS.sleep(1);
        rpcClient.shutdown();
    }
}
