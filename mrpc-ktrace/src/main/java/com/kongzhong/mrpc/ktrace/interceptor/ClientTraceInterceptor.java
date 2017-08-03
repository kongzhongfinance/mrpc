package com.kongzhong.mrpc.ktrace.interceptor;

import com.kongzhong.finance.basic.trace.Trace;
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
        Map<String, String> context = request.getContext();
        String traceId = Trace.getCurrentRequestId();
        context.put(TraceConstants.TRACE_ID, traceId);
        if (log.isDebugEnabled()) {
            log.debug("ClientTraceInterceptor [put] BeforeTraceId={} CurrentTraceId={}", Trace.getCurrentRequestId(), traceId);
        }

        // invoke
        return invoker.invoke();
    }
}
