package com.kongzhong.mrpc.server.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.service.PayService;
import com.kongzhong.mrpc.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author biezhi
 *         16/06/2017
 */
@RpcService
public class PayServiceImpl implements PayService {

    @Autowired
    private UserService userService;

    @Override
    public String pay(String msg) {
        System.out.println("完成支付操作");
        return userService.hello("调用hello");
    }

}
