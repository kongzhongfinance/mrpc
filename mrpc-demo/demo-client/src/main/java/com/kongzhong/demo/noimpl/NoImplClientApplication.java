package com.kongzhong.demo.noimpl;

import com.kongzhong.mrpc.client.RpcClient;
import com.kongzhong.mrpc.demo.service.NoImplService;

/**
 * 没有服务实现超时测试
 *
 * @author biezhi
 *         2017/6/12
 */
public class NoImplClientApplication {

    public static void main(String[] args) throws Exception {

        RpcClient rpcClient = new RpcClient();
        final NoImplService noImplService = rpcClient.getProxyBean(NoImplService.class);
        System.out.println(noImplService);
        noImplService.say();
        rpcClient.stop();
    }

}
