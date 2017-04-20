package com.kongzhong.mrpc.test.helloworld;

import com.kongzhong.mrpc.client.RpcClient;
import com.kongzhong.mrpc.test.service.CalcService;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ClientApplication {
    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("127.0.0.1:5066");

        CalcService calcService = rpcClient.execute(CalcService.class);
        System.out.println(calcService);
        System.out.println(calcService.add(10, 20));
        rpcClient.stop();
    }
}
