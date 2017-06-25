package com.kongzhong.mrpc.service;

import org.springframework.stereotype.Service;

/**
 * @author biezhi
 *         25/06/2017
 */
@Service
public class DemoService {

    public String hello(String msg) {
        return "mrpc-response-" + msg;
    }

}
