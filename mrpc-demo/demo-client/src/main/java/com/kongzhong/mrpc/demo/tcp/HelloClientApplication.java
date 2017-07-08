package com.kongzhong.mrpc.demo.tcp;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.model.StatusEnum;
import com.kongzhong.mrpc.demo.service.UserService;

import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 *         2017/4/19
 */
public class HelloClientApplication {

    public static void main(String[] args) throws Exception {

        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setDirectAddress("127.0.0.1:5066");

        final UserService userService = rpcClient.getProxyReferer(UserService.class);
//        TimeUnit.SECONDS.sleep(2);

        System.out.println(userService);

        StatusEnum statusEnum = userService.testEnum(StatusEnum.SUCCESS);
        System.out.println(statusEnum);

        System.out.println(userService.getPersons().get(0).getClass());

        System.out.println(userService.getResult().getData().getClass());

        int pos = 1;
        while (pos < 10_0000) {
            System.out.println(userService.add(10, pos++));
            TimeUnit.SECONDS.sleep(3);
        }

        rpcClient.shutdown();
    }
}
