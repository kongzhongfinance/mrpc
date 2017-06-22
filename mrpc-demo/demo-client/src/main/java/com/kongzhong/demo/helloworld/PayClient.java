package com.kongzhong.demo.helloworld;

import com.kongzhong.mrpc.client.RpcClient;
import com.kongzhong.mrpc.demo.service.PayService;

import java.math.BigDecimal;

/**
 * @author biezhi
 *         2017/4/19
 */
public class PayClient {

    public static void main(String[] args) throws Exception {

        RpcClient rpcClient = new RpcClient();

        final PayService payService = rpcClient.getProxyReferer(PayService.class);
        System.out.println(payService.pay("pay call", new BigDecimal("2201.1")));
        rpcClient.stop();
    }
}
