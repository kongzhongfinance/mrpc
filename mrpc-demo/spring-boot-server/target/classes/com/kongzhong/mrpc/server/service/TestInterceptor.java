package com.kongzhong.mrpc.server.service;

import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author biezhi
 *         2017/4/24
 */
@Slf4j
public class TestInterceptor implements RpcInteceptor {

    @Override
    public Object execute(Invocation invocation) throws Exception {
        log.info("test interceptor execute before.");
        Object obj = invocation.next();
        log.info("test interceptor execute after.");
        return obj;
    }
}
