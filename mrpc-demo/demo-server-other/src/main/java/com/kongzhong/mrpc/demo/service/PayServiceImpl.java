package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.annotation.RpcService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author biezhi
 *         2017/4/28
 */
@RpcService
public class PayServiceImpl implements PayService {

    @Autowired
    private UserService userService;

    @Override
    public String pay(String msg) {
        return userService.hello(msg);
    }

}
