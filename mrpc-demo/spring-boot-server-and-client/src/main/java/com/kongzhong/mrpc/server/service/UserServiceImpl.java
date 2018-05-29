package com.kongzhong.mrpc.server.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.service.UserService;

/**
 * @author biezhi
 * @date 2018/5/29
 */
@RpcService
public class UserServiceImpl implements UserService {

    @Override
    public String testTrace() {
        return null;
    }

    @Override
    public String testServerCustomException() {
        return null;
    }
}
