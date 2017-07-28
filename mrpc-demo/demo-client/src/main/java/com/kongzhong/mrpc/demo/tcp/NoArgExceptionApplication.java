package com.kongzhong.mrpc.demo.tcp;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.service.UserService;

/**
 * 没有构造函数的异常测试
 *
 * @author biezhi
 *         2017/6/12
 */
public class NoArgExceptionApplication {

    public static void main(String[] args) throws Exception {

        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setDirectAddress("127.0.0.1:5066");
//        rpcClient.setTransport("http");

        UserService userService = rpcClient.getProxyReferer(UserService.class);
        try {
            userService.testNoArgException();
        } catch (Exception e) {
            e.printStackTrace();
        }
        rpcClient.shutdown();
    }

}
