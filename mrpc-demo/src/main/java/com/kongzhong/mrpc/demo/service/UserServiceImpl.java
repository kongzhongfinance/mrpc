package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.model.Person;

import java.util.List;
import java.util.Map;

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
    public Person save(Person person) {
        return person;
    }

    @Override
    public Long delete(Long id) {
        return id;
    }

    @Override
    public List<String> strList(List<String> strs) {
        return strs;
    }

    @Override
    public Map toMap(Map<String, Integer> map) {
        return map;
    }
}