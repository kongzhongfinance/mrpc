package com.kongzhong.mrpc.trace.interceptor;

import com.google.common.base.Stopwatch;
import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.basic.zipkin.agent.KafkaAgent;
import com.kongzhong.basic.zipkin.util.AppConfiguration;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.trace.config.TraceServerAutoConfigure;
import com.kongzhong.mrpc.utils.Ids;
import com.kongzhong.mrpc.utils.NetUtils;
import com.kongzhong.mrpc.utils.TimeUtils;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    public Object execute(ServerInvocation invocation) throws Throwable {
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

        // start the watch
        Stopwatch watch = Stopwatch.createStarted();

        request.addContext(TraceConstants.SR_TIME, String.valueOf(TimeUtils.currentMicros()));

        // start tracing
        TraceContext.start();

        // prepare trace context
        Span span = startTrace(request);

        TraceContext.setTraceId(span.getTrace_id());
        TraceContext.setSpanId(span.getId());

        TraceContext.print();

        try {
            Object result = invocation.next();

            this.endTrace(request, span, watch);
            request.getContext().put(TraceConstants.SS_TIME, String.valueOf(TimeUtils.currentMicros()));
            return result;
        } catch (Exception e) {
            this.endTrace(request, span, watch);
            throw e;
        }
    }

    private Span startTrace(RpcRequest request) {
        Map<String, String> attaches = request.getContext();
        // start provider span
        Span providerSpan = new Span();
        providerSpan.setId(Ids.get());
        long traceId      = Long.parseLong(attaches.get(TraceConstants.TRACE_ID));
        long parentSpanId = Long.parseLong(attaches.get(TraceConstants.SPAN_ID));
        providerSpan.setTrace_id(traceId);
        providerSpan.setParent_id(parentSpanId);

        String serviceName = RequestUtils.getServerName(request.getClassName(), request.getMethodName());
        providerSpan.setName(serviceName);

        int providerHost = NetUtils.ip2Num(request.getContext().get(Const.SERVER_HOST));
        int providerPort = Integer.parseInt(request.getContext().get(Const.SERVER_PORT));

        // sr annotation
        providerSpan.addToAnnotations(
                Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_SR,
                        Endpoint.create(serviceName, providerHost, providerPort)));

        return providerSpan;
    }

    private void endTrace(RpcRequest request, Span span, Stopwatch watch) {
        try {
            span.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));

            String serviceName = RequestUtils.getServerName(request.getClassName(), request.getMethodName());

            int providerHost = NetUtils.ip2Num(request.getContext().get(Const.SERVER_HOST));
            int providerPort = Integer.parseInt(request.getContext().get(Const.SERVER_PORT));

            // ss annotation
            span.addToAnnotations(
                    Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_SS,
                            Endpoint.create(serviceName, providerHost, providerPort)));

            TraceContext.addSpan(span);

            // collect the span
            TraceContext.addSpan(span);
            agent.send(TraceContext.getSpans());

            if (log.isDebugEnabled()) {
                log.debug("Server Send trace data {}.", JacksonSerialize.toJSONString(TraceContext.getSpans()));
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