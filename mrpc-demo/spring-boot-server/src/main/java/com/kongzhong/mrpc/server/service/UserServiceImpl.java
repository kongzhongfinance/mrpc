package com.kongzhong.mrpc.server.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.service.BenchmarkService;
import com.kongzhong.mrpc.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author biezhi
 * 2017/4/19
 */
@Slf4j
@RpcService
public class UserServiceImpl implements UserService {

    @Autowired
    private BenchmarkService benchmarkService;

    @Override
    public String testTrace() {
        log.info("####testTrace####");
        return benchmarkService.echoService("hello").toString();
    }
}