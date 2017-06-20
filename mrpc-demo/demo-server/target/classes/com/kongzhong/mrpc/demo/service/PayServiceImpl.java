package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.model.NoConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * @author biezhi
 *         2017/4/28
 */
@RpcService
public class PayServiceImpl implements PayService {

    @Autowired
    private UserService userService;

    @Override
    public String pay(String msg, BigDecimal money) {
        System.out.println(money);
        return userService.hello(msg);
    }

    @Override
    public BigDecimal getMoney(Double money) {
        return new BigDecimal(money.toString());
    }

    @Override
    public NoConstructor noConstructor(NoConstructor noConstructor) {
        System.out.println(noConstructor);
        return noConstructor;
    }

}
