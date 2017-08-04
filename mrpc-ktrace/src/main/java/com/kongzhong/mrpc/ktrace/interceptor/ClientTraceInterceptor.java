package com.kongzhong.mrpc.ktrace.interceptor;

import com.kongzhong.finance.ktrace.core.Trace;
import com.kongzhong.mrpc.client.invoke.ClientInvocation;
import com.kongzhong.mrpc.client.invoke.RpcInvoker;
import com.kongzhong.mrpc.interceptor.RpcClientInterceptor;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.trace.TraceConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Created by IFT8 on 2017/8/1.
 */
@Slf4j
public class ClientTraceInterceptor implements RpcClientInterceptor {

    @Override
    public Object execute(ClientInvocation invocation) throws Exception {
        RpcInvoker invoker = invocation.getRpcInvoker();
        RpcRequest request = invoker.getRequest();

        // prepare trace context
        String traceId = Trace.getCurrentRequestId();
        // put requestContext
        Map<String, String> context = request.getContext();
        context.put(TraceConstants.TRACE_ID, traceId);

        log.debug("ClientTraceInterceptor [put] CurrentTraceId={}", traceId);

        // invoke
        return invoker.invoke();
    }
}
