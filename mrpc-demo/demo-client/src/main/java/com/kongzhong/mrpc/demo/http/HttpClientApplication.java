package com.kongzhong.mrpc.demo.http;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.model.Person;
import com.kongzhong.mrpc.demo.model.XXDto;
import com.kongzhong.mrpc.demo.service.UserService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author biezhi
 *         2017/4/19
 */
public class HttpClientApplication {

    public static void main(String[] args) throws InterruptedException {
        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setDirectAddress("127.0.0.1:5066");
        rpcClient.setTransport("http");

        UserService userService = rpcClient.getProxyReferer(UserService.class);

        Thread.sleep(1000);

        userService.hello("hello world http.");

        int sum = userService.add(10, 20);

        userService.testArray(new String[]{"a", "b", "c"});

        System.out.println("add => " + sum);

        Person person = new Person();
        person.setName("王爵nice");

        Person person1 = userService.savePerson("hihihi", 99);
        System.out.println("save result = " + person1);

        Map<String, Integer> map = new HashMap<>();
        map.put("Hello", 22);
        Map<String, Integer> rmap = userService.toMap(map);
        System.out.println("toMap => " + rmap);

        System.out.println(userService.getPersons());

        XXDto xxDto = new XXDto();
        xxDto.setAge(22);
        xxDto.setBirthday(new Date());
        xxDto.setMoney(new BigDecimal("23.11"));

        System.out.println(userService.transDate(xxDto));

        rpcClient.shutdown();
    }
}
