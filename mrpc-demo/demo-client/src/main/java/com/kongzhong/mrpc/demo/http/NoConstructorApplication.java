package com.kongzhong.mrpc.demo.http;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.model.NoConstructor;
import com.kongzhong.mrpc.demo.service.PayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * @author biezhi
 *         2017/4/19
 */
public class NoConstructorApplication {

    public static final Logger log = LoggerFactory.getLogger(NoConstructorApplication.class);

    public static void main(String[] args) throws Exception {
        RpcSpringClient rpcClient = new RpcSpringClient();
        final PayService payService = rpcClient.getProxyReferer(PayService.class);
        NoConstructor noConstructor = payService.noConstructor(new NoConstructor("王大锤"));
        System.out.println(noConstructor);

        String msg = payService.pay("hello", new BigDecimal("20"));
        System.out.println(msg);
        rpcClient.shutdown();
    }

}
