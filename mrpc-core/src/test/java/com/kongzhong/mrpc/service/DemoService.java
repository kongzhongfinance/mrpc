package com.kongzhong.mrpc.service;

/**
 * 演示服务
 *
 * @author biezhi
 *         25/06/2017
 */
public class DemoService {

    public String hello(String msg) {
        return "mrpc-response-" + msg;
    }

}
