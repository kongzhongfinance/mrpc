package com.kongzhong.mrpc.trace.interceptor;

import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.basic.zipkin.agent.KafkaAgent;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.trace.config.TraceServerAutoConfigure;
import com.kongzhong.mrpc.utils.TimeUtils;
import com.twitter.zipkin.gen.Span;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
            this.traceServerAutoConfigure = new TraceServerAutoConfigure();
        } else {
            try {
                this.agent = new KafkaAgent(traceServerAutoConfigure.getUrl(), traceServerAutoConfigure.getTopic());
            } catch (Exception e) {
                log.error("初始化Trace服务端失败", e);
            }
        }
    }

    @Override
    public Object execute(ServerInvocation invocation) throws Exception {
        if (!this.traceServerAutoConfigure.getEnable()) {
            // not enable tracing
            return invocation.next();
        }

        log.debug("Trace Server Interceptor");

        RpcRequest request = invocation.getRequest();
        String     traceId = request.getContext().get(TraceConstants.TRACE_ID);
        if (null == traceId) {
            // don't need tracing
            return invocation.next();
        }

        // prepare trace context
        startTrace(request.getContext());

        TraceContext.print();

        try {
            Object result = invocation.next();
            request.getContext().put(TraceConstants.SS_TIME, String.valueOf(TimeUtils.currentMicros()));
            this.endTrace();
            return result;
        } catch (Exception e) {
            this.endTrace();
            throw e;
        }
    }

    private void startTrace(Map<String, String> attaches) {

        long traceId      = Long.parseLong(attaches.get(TraceConstants.TRACE_ID));
        long parentSpanId = Long.parseLong(attaches.get(TraceConstants.SPAN_ID));

        // start tracing
        TraceContext.start();
        TraceContext.setTraceId(traceId);
        TraceContext.setSpanId(parentSpanId);

    }

    private void endTrace() {
        try {
            List<Span> spans = TraceContext.getSpans();
            agent.send(spans);
            if (log.isDebugEnabled()) {
                log.debug("Server Send trace data {}.", JacksonSerialize.toJSONString(spans));
            }
        } catch (Exception e) {
            log.error("Server 发送Trace失败", e);
        }
        TraceContext.clear();
        if (log.isDebugEnabled()) {
            log.debug("Server Trace clear.");
            TraceContext.print();
        }
    }


}