package com.kongzhong.mrpc.demo.http;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
public class ExceptionApplication {

    public static void main(String[] args) throws InterruptedException {
        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setTransport("http");
        rpcClient.setDirectAddress("127.0.0.1:5066");

        UserService userService = rpcClient.getProxyReferer(UserService.class);

        try {
//             userService.testNormalExp();
//            userService.testBizExp(2333);
            userService.testNoArgException();
        } catch (Exception e) {
            e.printStackTrace();
        }

        rpcClient.shutdown();
    }
}
