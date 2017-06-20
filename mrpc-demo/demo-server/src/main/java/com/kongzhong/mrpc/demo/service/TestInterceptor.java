package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcServerInteceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author biezhi
 *         2017/4/24
 */
@Slf4j
public class TestInterceptor implements RpcServerInteceptor {

    @Override
    public Object execute(ServerInvocation invocation) throws Exception {
        log.info("test interceptor execute before.");
        Object obj = invocation.next();
        log.info("test interceptor execute after.");
        return obj;
    }
}
