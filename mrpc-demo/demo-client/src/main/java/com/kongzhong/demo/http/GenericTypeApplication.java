package com.kongzhong.demo.http;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.model.Person;
import com.kongzhong.mrpc.demo.service.UserService;

import java.util.List;

/**
 * 泛型入参，返回值测试
 *
 * @author biezhi
 *         2017/4/19
 */
public class GenericTypeApplication {

    public static void main(String[] args) {

        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setDirectAddress("127.0.0.1:5066");
        rpcClient.setTransport("http");

        UserService userService = rpcClient.getProxyReferer(UserService.class);
        List<Person> peoples = userService.getPersons();

        peoples.forEach(person -> System.out.println(person.getName()));

        userService.setPersons(peoples);

        rpcClient.shutdown();
    }
}
