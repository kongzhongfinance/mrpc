package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.spring.annotation.GET;
import com.kongzhong.mrpc.spring.annotation.MRpcService;

/**
 * @author biezhi
 *         2017/4/19
 */
@MRpcService(path = "/")
public interface CalcService {

    int add(int a, int b);

    @GET("hello")
    String hello(String name);

    @GET("hello")
    String hello(Integer age);

}