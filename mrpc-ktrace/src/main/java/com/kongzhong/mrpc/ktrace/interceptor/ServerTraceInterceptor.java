package com.kongzhong.mrpc.ktrace.interceptor;

import com.kongzhong.finance.basic.trace.Trace;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import com.kongzhong.mrpc.model.RpcRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by IFT8 on 2017/8/1.
 */
@Slf4j
public class ServerTraceInterceptor implements RpcServerInterceptor {

    @Override
    public Object execute(ServerInvocation invocation) throws Exception {
        RpcRequest request = invocation.getRequest();

        System.out.println(Thread.currentThread().getName());
        // prepare trace context
        String requestId = request.getContext().get("requestId");
        if (log.isDebugEnabled()) {
            log.debug("ServerTraceInterceptor CurrentRequestId={} AfterRequestId={}", Trace.getCurrentRequestId(), requestId);
        }
        Trace.continueTrace(requestId);

        // executor other interceptor
        return invocation.next();
    }
}