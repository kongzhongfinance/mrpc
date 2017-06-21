package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.exception.BizException;
import com.kongzhong.mrpc.demo.model.Person;
import com.kongzhong.mrpc.demo.model.Result;
import com.kongzhong.mrpc.demo.model.StatusEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author biezhi
 *         2017/4/19
 */
@RpcService
public class UserServiceImpl implements UserService {

    public UserServiceImpl() {

    }

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

    @Override
    public void testArray(String[] strs) {
        System.out.println("接收到：" + Arrays.toString(strs));
    }

    @Override
    public void testBizExp(Integer num) throws BizException {
        num.compareTo(222);
        throw new BizException(1200, "xx对象不能为空");
    }

    @Override
    public void testNormalExp() throws Exception {
        throw new Exception("我是一个异常");
    }

    @Override
    public StatusEnum testEnum(StatusEnum statusEnum) {
        System.out.println("接收到枚举：" + statusEnum);
        return statusEnum;
    }

    @Override
    public List<Person> getPersons() {
        Person person = new Person();
        person.setName("jack");
        List<Person> list = new ArrayList<>();
        list.add(person);
        return list;
    }

    @Override
    public void setPersons(List<Person> persons) {
        System.out.println(persons);
    }

    @Override
    public Result<Person> getResult() {
        Result<Person> personResult = new Result<>();
        Person person = new Person();
        person.setName("jack");
        personResult.setData(person);
        return personResult;
    }
}