package com.kongzhong.mrpc.server.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.service.BenchmarkService;
import com.kongzhong.mrpc.demo.service.OtherService;
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

    @Autowired
    private OtherService otherService;

    @Override
    public String testTrace() {
        log.info("进入 UserService");
        log.info("开始调用 OtherService");
//        String s = otherService.waitTime(3);
//        return benchmarkService.echoService("hello:" + s).toString();
        return "aaa";
    }

}