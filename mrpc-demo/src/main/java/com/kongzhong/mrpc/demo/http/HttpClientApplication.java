package com.kongzhong.mrpc.demo.http;

import com.kongzhong.mrpc.client.RpcClient;
import com.kongzhong.mrpc.demo.model.Person;
import com.kongzhong.mrpc.demo.service.CalcService;

/**
 * @author biezhi
 *         2017/4/19
 */
public class HttpClientApplication {

    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("127.0.0.1:5066");
        rpcClient.setTransfer("http");

        CalcService calcService = rpcClient.getProxyBean(CalcService.class);
        Person person = new Person();
        person.setName("王爵nice");

        boolean result = calcService.savePerson(person, 99);
        System.out.println("result = " + result);

        rpcClient.stop();
    }
}
