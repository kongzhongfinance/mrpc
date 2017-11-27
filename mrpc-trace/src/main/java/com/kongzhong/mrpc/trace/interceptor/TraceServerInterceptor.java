package com.kongzhong.mrpc.trace.interceptor;

import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.basic.zipkin.agent.KafkaAgent;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.trace.TraceConstants;

import com.kongzhong.mrpc.trace.config.TraceServerAutoConfigure;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * ServerTraceInterceptor
 */
@Slf4j
public class TraceServerInterceptor implements RpcServerInterceptor {

    private AbstractAgent agent;

    @Resource
    private TraceServerAutoConfigure traceServerAutoConfigure;

    @PostConstruct
    public void init() {
        if (null == traceServerAutoConfigure) {
            traceServerAutoConfigure = new TraceServerAutoConfigure();
        }
        log.info("Server {}", traceServerAutoConfigure);
        this.agent = new KafkaAgent(traceServerAutoConfigure.getUrl());
    }

    @Override
    public Object execute(ServerInvocation invocation) throws Exception {
        if (!traceServerAutoConfigure.getEnable()) {
            // not enable tracing
            return invocation.next();
        }

        RpcRequest request = invocation.getRequest();
        String     traceId = request.getContext().get(TraceConstants.TRACE_ID);
        if (null == traceId) {
            // don't need tracing
            return invocation.next();
        }

        String spanId = request.getContext().get(TraceConstants.SPAN_ID);

        log.info("traceId : {}", traceId);
        // prepare trace context
        this.startTrace(traceId, spanId);
        try {
            Object result = invocation.next();
            this.endTrace();
            return result;
        } catch (Exception e) {
            this.endTrace();
            throw e;
        }
    }

    private void startTrace(String traceId, String spanId) {
        // start tracing
        TraceContext.start();
        TraceContext.setTraceId(Long.parseLong(traceId));
        TraceContext.setSpanId(Long.parseLong(spanId));
    }

    private void endTrace() {
        try {
            agent.send(TraceContext.getSpans());
            TraceContext.clear();
        } catch (Exception e) {
            log.error("", e);
        }
    }


}