package com.kongzhong.mrpc.trace.interceptor;

import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.trace.TraceAgent;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.trace.TraceContext;
import com.kongzhong.mrpc.trace.config.TraceConf;
import com.kongzhong.mrpc.trace.config.TraceConfLoader;

/**
 * ServerTraceInterceptor
 */
public class ServerTraceInterceptor implements RpcServerInterceptor {

    private TraceConf  conf  = TraceConfLoader.load("trace.yml");
    private TraceAgent agent = new TraceAgent(conf.getServer());

    @Override
    public Object execute(ServerInvocation invocation) throws Exception {
        if (!conf.getEnable()) {
            // not enable tracing
            return invocation.next();
        }

        RpcRequest request = invocation.getRequest();

        String traceId = request.getContext().get(TraceConstants.TRACE_ID);
        if (null == traceId) {
            // don't need tracing
            return invocation.next();
        }

        String spanId = request.getContext().get(TraceConstants.SPAN_ID);
        // prepare trace context
        this.startTrace(traceId, spanId);
        //

        Object result = invocation.next();
        this.endTrace();
        return result;
    }

    private void startTrace(String traceId, String spanId) {
        // start tracing
        TraceContext.start();
        TraceContext.setTraceId(Long.parseLong(traceId));
        TraceContext.setSpanId(Long.parseLong(spanId));
    }

    private void endTrace() {
        agent.send(TraceContext.getSpans());
        TraceContext.clear();
    }

}