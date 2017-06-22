package com.kongzhong.demo.tcp.registry;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.service.UserService;
import com.kongzhong.mrpc.discover.ZookeeperServiceDiscovery;

/**
 * @author biezhi
 *         2017/4/19
 */
public class DiscoveryClientApplication {
    public static void main(String[] args) throws Exception {

        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setDefaultDiscovery(new ZookeeperServiceDiscovery("127.0.0.1:2181"));
        rpcClient.setAppId("demo");
        UserService userService = rpcClient.getProxyReferer(UserService.class);
        int index = 1;
        while (true) {
            System.out.println(userService.add(10, index++));
            Thread.sleep(3000);
        }
    }
}
