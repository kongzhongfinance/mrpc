package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.model.Person;
import com.kongzhong.mrpc.model.RpcRet;

/**
 * @author biezhi
 *         2017/4/19
 */
@RpcService
public class UserServiceImpl implements UserService {

    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public String hello(String name) {
        return "Hello, " + name;
    }

    @Override
    public Person savePerson(String fullName, Integer age) {
        Person person = new Person();
        person.setName(fullName);
        return person;
    }

    @Override
    public RpcRet delete(Long id) {
        return RpcRet.ok(id);
    }
}