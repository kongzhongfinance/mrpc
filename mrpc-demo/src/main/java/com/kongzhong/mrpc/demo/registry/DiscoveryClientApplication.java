package com.kongzhong.mrpc.demo.registry;

import com.kongzhong.mrpc.client.RpcClient;
import com.kongzhong.mrpc.demo.service.CalcService;
import com.kongzhong.mrpc.discover.ZookeeperServiceDiscovery;

import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 *         2017/4/19
 */
public class DiscoveryClientApplication {
    public static void main(String[] args) throws Exception {

        RpcClient rpcClient = new RpcClient(new ZookeeperServiceDiscovery("127.0.0.1:2181"));
        CalcService calcService = rpcClient.getProxyBean(CalcService.class);
        while (true) {
            System.out.println(calcService.add(10, 20));
            Thread.sleep(3000);
        }
    }
}
