package com.kongzhong.demo.metrics;

import com.kongzhong.mrpc.client.RpcClient;
import com.kongzhong.mrpc.demo.service.UserService;

/**
 * @author biezhi
 *         2017/4/19
 */
public class MetricsClientApplication {

    public static void main(String[] args) throws Exception {

        RpcClient rpcClient = new RpcClient();

        final UserService userService = rpcClient.getProxyBean(UserService.class);
        int pos = 1;
        while (true) {
            userService.add(10, pos++);
        }
    }
}
