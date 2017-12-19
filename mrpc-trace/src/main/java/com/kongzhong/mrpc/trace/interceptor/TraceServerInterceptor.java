package com.kongzhong.mrpc.trace.interceptor;

import com.google.common.base.Stopwatch;
import com.kongzhong.basic.zipkin.TraceConstants;
import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.basic.zipkin.agent.InitializeAgent;
import com.kongzhong.basic.zipkin.util.AppConfiguration;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.trace.config.TraceAutoConfigure;
import com.kongzhong.mrpc.trace.utils.RequestUtils;
import com.kongzhong.mrpc.utils.Ids;
import com.kongzhong.mrpc.utils.NetUtils;
import com.kongzhong.mrpc.utils.TimeUtils;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ServerTraceInterceptor
 */
@Slf4j
public class TraceServerInterceptor implements RpcServerInterceptor {

    private AbstractAgent agent;

    private TraceAutoConfigure traceAutoConfigure;

    public TraceServerInterceptor(TraceAutoConfigure traceAutoConfigure) {
        this.traceAutoConfigure = traceAutoConfigure;
        if (null == traceAutoConfigure) {
            this.traceAutoConfigure = new TraceAutoConfigure();
        } else {
            this.traceAutoConfigure = traceAutoConfigure;
            AbstractAgent agent = InitializeAgent.getAgent();
            if (null == agent) {
                this.agent = InitializeAgent.initAndGetAgent(traceAutoConfigure.getUrl(), traceAutoConfigure.getTopic());
            } else {
                this.agent = agent;
            }
        }
        log.info("TraceServerInterceptor 初始化完毕 config={}", this.traceAutoConfigure);
    }

    @Override
    public Object execute(ServerInvocation invocation) throws Throwable {
        if (!this.traceAutoConfigure.getEnable()) {
            // not enable tracing
            return invocation.next();
        }

        log.debug("Trace Server Interceptor");

        RpcRequest request = invocation.getRequest();
        String traceId = request.getContext() == null ? null : request.getContext().get(TraceConstants.TRACE_ID);
        if (null == traceId) {
            // don't need tracing
            return invocation.next();
        }

        Stopwatch watch = Stopwatch.createStarted();

        // prepare trace context
        Span span = startTrace(request);

        TraceContext.print();

        try {
            Object result = invocation.next();
            request.getContext().put(TraceConstants.SS_TIME, String.valueOf(TimeUtils.currentMicros()));
            this.endTrace(request, span, watch);
            return result;
        } catch (Exception e) {
            this.endTrace(request, span, watch);
            throw e;
        }
    }

    private Span startTrace(RpcRequest rpcRequest) {
        Map<String, String> attaches = rpcRequest.getContext();

        long traceId = Long.parseLong(attaches.get(TraceConstants.TRACE_ID));
        long parentSpanId = Long.parseLong(attaches.get(TraceConstants.SPAN_ID));

        // start tracing

        Span span = new Span();
        long timestamp = TimeUtils.currentMicros();

        span.setId(Ids.get());
        span.setTrace_id(traceId);
        span.setParent_id(parentSpanId);
        span.setName(RequestUtils.getServerName(rpcRequest.getClassName(), rpcRequest.getMethodName()));
        span.setTimestamp(timestamp);

        // sr annotation
        int providerHost = NetUtils.ip2Num(attaches.get(Const.SERVER_HOST));
        int providerPort = Integer.parseInt(attaches.get(Const.SERVER_PORT));

        span.addToAnnotations(
                Annotation.create(timestamp, TraceConstants.ANNO_SR,
                        Endpoint.create(AppConfiguration.getAppId(), providerHost, providerPort)));

        TraceContext.addSpanAndUpdate(span);
        return span;
    }

    private void endTrace(RpcRequest rpcRequest, Span span, Stopwatch watch) {
        try {
            Map<String, String> attaches = rpcRequest.getContext();

            if (span == null) {
                return;
            }
            span.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));

            // ss annotation
            int providerHost = NetUtils.ip2Num(attaches.get(Const.SERVER_HOST));
            int providerPort = Integer.parseInt(attaches.get(Const.SERVER_PORT));

            span.addToAnnotations(
                    Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_SS,
                            Endpoint.create(AppConfiguration.getAppId(), providerHost, providerPort)));

            List<Span> spans = TraceContext.getSpans();
            agent.send(spans);
            if (log.isDebugEnabled()) {
                log.debug("Server Send trace data {}.", JacksonSerialize.toJSONString(spans));
            }
        } catch (Exception e) {
            log.error("Server发送Trace失败", e);
        }
        TraceContext.clear();
        if (log.isDebugEnabled()) {
            log.debug("TraceServerInterceptor Trace clear. traceId={}", TraceContext.getTraceId());
            TraceContext.print();
        }
    }


}