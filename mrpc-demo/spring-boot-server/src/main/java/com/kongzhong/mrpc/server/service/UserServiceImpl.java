package com.kongzhong.mrpc.server.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.model.NoConstructor;
import com.kongzhong.mrpc.demo.service.BenchmarkService;
import com.kongzhong.mrpc.demo.service.PayService;
import com.kongzhong.mrpc.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * @author biezhi
 * 2017/4/19
 */
@Slf4j
@RpcService
public class UserServiceImpl implements UserService, PayService {

    @Autowired
    private BenchmarkService benchmarkService;

    @Override
    public String testTrace() {
        log.info("####testTrace####");
        return benchmarkService.echoService("hello").toString();
    }

    @Override
    public String testServerCustomException() {
        return "23333";
    }

    @Override
    public String pay(String msg, BigDecimal money) {
        return null;
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