package com.kongzhong.mrpc.server.service;

import com.kongzhong.mrpc.interceptor.*;
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
