package com.kongzhong.mrpc.ktrace.interceptor;

import com.kongzhong.finance.ktrace.core.Trace;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.trace.TraceConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by IFT8 on 2017/8/1.
 */
@Slf4j
public class ServerTraceInterceptor implements RpcServerInterceptor {

    @Override
    public Object execute(ServerInvocation invocation) throws Exception {
        RpcRequest request = invocation.getRequest();

        // prepare trace context
        String traceId = request.getContext().get(TraceConstants.TRACE_ID);
        if (log.isDebugEnabled()) {
            log.debug("ServerTraceInterceptor CurrentTraceId={} AfterTraceId={}", Trace.getCurrentRequestId(), traceId);
        }
        Trace.continueTrace(traceId, Trace.parseParentSpanId(traceId));

        // executor other interceptor
        return invocation.next();
    }
}