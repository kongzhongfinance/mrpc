package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.demo.model.Person;
import com.kongzhong.mrpc.model.RpcRet;

/**
 * @author biezhi
 *         2017/4/19
 */
public interface UserService {

    int add(int a, int b);

    String hello(String name);

    Person savePerson(String fullName, Integer age);

    RpcRet delete(Long id);
}