package com.kongzhong.demo.helloworld;

import com.kongzhong.mrpc.client.RpcClient;
import com.kongzhong.mrpc.demo.model.StatusEnum;
import com.kongzhong.mrpc.demo.service.UserService;

import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ClientApplication {

    public static void main(String[] args) throws Exception {

        RpcClient rpcClient = new RpcClient();
        rpcClient.setDirectAddress("127.0.0.1:5066");

        final UserService userService = rpcClient.getProxyReferer(UserService.class);
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

//        ExecutorService executorService = Executors.newCachedThreadPool();
//        for (int i = 0; i < 20; i++) {
//            executorService.submit(new Runnable() {
//                @Override
//                public void run() {
//                    for (int i = 0; i < 1000000000; i++) {
//                        String say = userService.hello("hi, " + i);
//                        if (i % 10000 == 0) {
//                            System.out.println(say);
//                        }
//                    }
//                }
//            });
//        }
        rpcClient.stop();
    }
}
