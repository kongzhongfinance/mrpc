package com.kongzhong.mrpc.server.service;

import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author biezhi
 *         2017/4/24
 */
@Slf4j
public class TestInterceptor implements RpcServerInterceptor {

    @Override
    public Object execute(ServerInvocation invocation) throws Throwable {
        log.info("test interceptor execute before.");
        Object obj = invocation.next();
        log.info("test interceptor execute after.");
        return obj;
    }
}
