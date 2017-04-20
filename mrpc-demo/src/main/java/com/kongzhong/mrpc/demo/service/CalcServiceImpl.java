package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.model.Person;

/**
 * @author biezhi
 *         2017/4/19
 */
@RpcService
public class CalcServiceImpl implements CalcService {

    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public String hello(String name) {
        return null;
    }

    @Override
    public boolean savePerson(Person person, Integer age) {
        System.out.println("person = " + person);
        System.out.println("age = " + age);
        return null != age && age > 18;
    }

}