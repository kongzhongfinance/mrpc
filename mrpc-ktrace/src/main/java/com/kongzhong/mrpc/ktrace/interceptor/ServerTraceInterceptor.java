package com.kongzhong.mrpc.ktrace.interceptor;

import com.kongzhong.finance.ktrace.core.Trace;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.trace.TraceConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Created by IFT8 on 2017/8/1.
 */
@Slf4j
public class ServerTraceInterceptor implements RpcServerInterceptor {

    @Override
    public Object execute(ServerInvocation invocation) throws Exception {
        RpcRequest request = invocation.getRequest();

        // TODO 操蛋的兼容
        if (null != request.getContext()) {
            // prepare trace context
            String traceId = request.getContext().get(TraceConstants.TRACE_ID);
            //rpc使用MDC方式接入 必须手动清空
            MDC.remove(TraceConstants.TRACE_ID);
            Trace.continueTrace(traceId, Trace.parseParentSpanId(traceId));
            if (log.isDebugEnabled()) {
                String currentRequestId = Trace.getCurrentRequestId();
                log.debug("ServerTraceInterceptor beforeTraceId={} CurrentTraceId={}", traceId, currentRequestId);
            }
        }

        // executor other interceptor
        // 拦截器顺序未知 所以不能清空Trace
        return invocation.next();
    }
}