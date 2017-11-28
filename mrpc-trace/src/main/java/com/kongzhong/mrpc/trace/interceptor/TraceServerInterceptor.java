package com.kongzhong.mrpc.trace.interceptor;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.basic.zipkin.agent.KafkaAgent;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.trace.config.TraceServerAutoConfigure;
import com.kongzhong.mrpc.utils.Ids;
import com.kongzhong.mrpc.utils.NetUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import com.kongzhong.mrpc.utils.TimeUtils;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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
            traceServerAutoConfigure = new TraceServerAutoConfigure();
        } else {
            this.agent = new KafkaAgent(traceServerAutoConfigure.getUrl());
        }
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

        // start the watch
        Stopwatch watch = Stopwatch.createStarted();

        request.getContext().put(TraceConstants.SR_TIME, String.valueOf(TimeUtils.currentMicros()));

        // start tracing
        TraceContext.start();

        // prepare trace context
        Span providerSpan = this.startTrace(request);
        TraceContext.setTraceId(providerSpan.getTrace_id());
        TraceContext.setSpanId(providerSpan.getId());

        try {
            Object result = invocation.next();
            this.endTrace(request, providerSpan, watch, null);
            request.getContext().put(TraceConstants.SS_TIME, String.valueOf(TimeUtils.currentMicros()));
            return result;
        } catch (Exception e) {
            this.endTrace(request, providerSpan, watch, e);
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
        providerSpan.setName(request.getMethodName());
        long timestamp = TimeUtils.currentMicros();
        providerSpan.setTimestamp(timestamp);

        // cs annotation
        int providerHost = NetUtils.ip2Num(request.getContext().get(Const.SERVER_HOST));
        int providerPort = Integer.parseInt(request.getContext().get(Const.SERVER_PORT));

        providerSpan.addToAnnotations(
                Annotation.create(timestamp, TraceConstants.ANNO_SR,
                        Endpoint.create(request.getClassName(), providerHost, providerPort)));

        String providerOwner = request.getContext().get(Const.SERVER_OWNER);
        if (!StringUtils.isNotEmpty(providerOwner)) {
            // app owner
            providerSpan.addToBinary_annotations(BinaryAnnotation.create(
                    "负责人", providerOwner, null
            ));
        }

        return providerSpan;
    }

    private void endTrace(RpcRequest request, Span providerSpan, Stopwatch watch, Exception e) {
        try {

            providerSpan.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));

            // cr annotation
            int providerHost = NetUtils.ip2Num(request.getContext().get(Const.SERVER_HOST));
            int providerPort = Integer.parseInt(request.getContext().get(Const.SERVER_PORT));
            providerSpan.addToAnnotations(
                    Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_SS,
                            Endpoint.create(providerSpan.getName(), providerHost, providerPort)));

            // exception catch
            if (e != null) {
                // attach exception
                providerSpan.addToBinary_annotations(BinaryAnnotation.create(
                        "Exception", Throwables.getStackTraceAsString(e), null
                ));
            }

            // collect the span
            TraceContext.addSpan(providerSpan);
            agent.send(TraceContext.getSpans());
            TraceContext.clear();
        } catch (Exception t) {
            log.error("", t);
        }
    }


}