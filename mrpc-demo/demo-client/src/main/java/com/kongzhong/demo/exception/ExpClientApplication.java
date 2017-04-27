package com.kongzhong.demo.exception;

import com.kongzhong.mrpc.client.RpcClient;
import com.kongzhong.mrpc.demo.service.UserService;

import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ExpClientApplication {

    public static void main(String[] args) throws Exception {
        RpcClient rpcClient = new RpcClient();
        final UserService userService = rpcClient.getProxyBean(UserService.class);
//        userService.testBizExp();
        userService.testNormalExp();
        rpcClient.stop();
    }
}
