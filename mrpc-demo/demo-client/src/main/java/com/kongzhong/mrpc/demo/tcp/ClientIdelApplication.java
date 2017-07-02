package com.kongzhong.mrpc.demo.tcp;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.model.StatusEnum;
import com.kongzhong.mrpc.demo.service.UserService;

import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ClientIdelApplication {

    public static void main(String[] args) throws Exception {

        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setDirectAddress("127.0.0.1:5066");

        final UserService userService = rpcClient.getProxyReferer(UserService.class);
        TimeUnit.SECONDS.sleep(10);

        System.out.println(userService);

        StatusEnum statusEnum = userService.testEnum(StatusEnum.SUCCESS);
        System.out.println(statusEnum);

        rpcClient.shutdown();
    }
}
