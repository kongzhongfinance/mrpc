package com.kongzhong.demo.tcp;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.service.NoImplService;

/**
 * 没有服务实现超时测试
 *
 * @author biezhi
 *         2017/6/12
 */
public class NoImplClientApplication {

    public static void main(String[] args) throws Exception {

        RpcSpringClient rpcClient = new RpcSpringClient();
        final NoImplService noImplService = rpcClient.getProxyReferer(NoImplService.class);
        System.out.println(noImplService);
        noImplService.say();
        rpcClient.shutdown();
    }

}
