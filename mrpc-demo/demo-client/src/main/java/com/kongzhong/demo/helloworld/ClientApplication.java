package com.kongzhong.demo.helloworld;

import com.kongzhong.mrpc.client.RpcClient;
import com.kongzhong.mrpc.demo.service.UserService;

import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ClientApplication {

    public static void main(String[] args) throws Exception {

        RpcClient rpcClient = new RpcClient("127.0.0.1:5066");

        final UserService userService = rpcClient.getProxyBean(UserService.class);
        System.out.println(userService);

        int pos = 1;
        while (pos < 10_0000) {
            System.out.println(userService.add(10, pos++));
            TimeUnit.SECONDS.sleep(2);
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
