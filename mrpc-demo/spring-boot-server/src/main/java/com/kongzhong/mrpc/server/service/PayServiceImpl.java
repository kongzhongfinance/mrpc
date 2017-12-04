package com.kongzhong.mrpc.server.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.model.NoConstructor;
import com.kongzhong.mrpc.demo.service.PayService;

import java.math.BigDecimal;

/**
 * @author biezhi
 * @date 2017/11/22
 */
@RpcService
public class PayServiceImpl implements PayService {

    @Override
    public String pay(String msg, BigDecimal money) {
        return msg;
    }

    @Override
    public BigDecimal getMoney(Double money) {
        return null;
    }

    @Override
    public NoConstructor noConstructor(NoConstructor noConstructor) {
        return null;
    }
}
