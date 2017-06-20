package com.kongzhong.mrpc.server.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.model.NoConstructor;
import com.kongzhong.mrpc.demo.service.PayService;
import com.kongzhong.mrpc.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * @author biezhi
 *         16/06/2017
 */
@RpcService
public class PayServiceImpl implements PayService {

    @Autowired
    private UserService userService;

    @Override
    public String pay(String msg, BigDecimal money) {
        System.out.println(money);
        System.out.println("完成支付操作");
        return userService.hello("调用hello");
    }

    @Override
    public BigDecimal getMoney(Double money) {
        return null;
    }

    @Override
    public NoConstructor noConstructor(NoConstructor noConstructor) {
        System.out.println(noConstructor);
        return noConstructor;
    }
}
