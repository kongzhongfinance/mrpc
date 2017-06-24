package com.kongzhong.demo.tcp;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.service.UserService;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ClientInterceptorApplication {

    static int pos = 0;

    public static void main(String[] args) {
        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setDirectAddress("127.0.0.1:5066");

        rpcClient.addInterceptor((invocation) -> {
            System.out.println(++pos + ". 执行客户端拦截器");
            return invocation.next();
        });

        UserService userService = rpcClient.getProxyReferer(UserService.class);
        for (int i = 0; i < 10; i++) {
            System.out.println(userService.add(10, 20));
        }
        rpcClient.shutdown();
    }
}
