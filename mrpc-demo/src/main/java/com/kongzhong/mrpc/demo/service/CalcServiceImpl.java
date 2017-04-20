package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.spring.annotation.MRpcService;

/**
 * @author biezhi
 *         2017/4/19
 */
@MRpcService
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
    public String hello(Integer age) {
        return null;
    }

}