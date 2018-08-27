package com.kongzhong.mrpc.demo.http;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.model.Person;
import com.kongzhong.mrpc.demo.model.XXDto;
import com.kongzhong.mrpc.demo.service.OtherService;
import com.kongzhong.mrpc.demo.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author biezhi
 *         2017/4/19
 */
public class HelloClientApplication {

    public static void main(String[] args) throws InterruptedException {
        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setDirectAddress("127.0.0.1:5069");

        OtherService proxyReferer = rpcClient.getProxyReferer(OtherService.class);

        String waitTime = proxyReferer.waitTime(5);
        System.out.println(waitTime);

        UserService userService = rpcClient.getProxyReferer(UserService.class);

        try {



//            userService.testTimeout(14);
            Optional<XXDto> xxDto = userService.testOptional();
            System.out.println(xxDto);
        } catch (Exception e){
            e.printStackTrace();
        }

//        while (true) {
//            System.out.println(userService.hello("hello world http."));
//            Thread.sleep(3000);
//        }

    }
}
