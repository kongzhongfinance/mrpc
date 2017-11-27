package com.kongzhong.mrpc.trace.interceptor;

import com.google.common.base.Stopwatch;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.trace.TraceContext;
import com.kongzhong.mrpc.trace.agent.AbstractAgent;
import com.kongzhong.mrpc.trace.agent.KafkaAgent;
import com.kongzhong.mrpc.trace.config.TraceClientAutoConfigure;
import com.kongzhong.mrpc.trace.util.ServerInfo;
import com.kongzhong.mrpc.utils.Ids;
import com.kongzhong.mrpc.utils.TimeUtils;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * TraceFilter
 */
@Slf4j
public class TraceFilter implements Filter {

    private TraceClientAutoConfigure clientAutoConfigure;
    private AbstractAgent            agent;

    public TraceFilter(TraceClientAutoConfigure clientAutoConfigure) {
        this.clientAutoConfigure = clientAutoConfigure;
        this.agent = new KafkaAgent(clientAutoConfigure.getUrl());
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        if (!clientAutoConfigure.getEnable()) {
            return;
        }
        agent = new KafkaAgent(clientAutoConfigure.getUrl());
        log.info("init the trace interceptor with config({}).", new Object[]{config});
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!clientAutoConfigure.getEnable()) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        String             uri = req.getRequestURI();

        // do trace
        Stopwatch watch = Stopwatch.createStarted();

        // start root span
        Span rootSpan = startTrace(req, uri);

        // prepare trace context
        TraceContext.start();
        TraceContext.setTraceId(rootSpan.getTrace_id());
        TraceContext.setSpanId(rootSpan.getId());
        TraceContext.addSpan(rootSpan);

        // executor other filters
        chain.doFilter(request, response);

        // end root span
        endTrace(req, rootSpan, watch);

        // clear trace context
        TraceContext.clear();
    }

    private Span startTrace(HttpServletRequest req, String point) {

        String apiName = req.getRequestURI();
        Span   apiSpan = new Span();

        // span basic data
        long id = Ids.get();
        apiSpan.setId(id);
        apiSpan.setTrace_id(id);
        apiSpan.setName(point);
        long timestamp = TimeUtils.currentMicros();
        apiSpan.setTimestamp(timestamp);

        // sr annotation
        apiSpan.addToAnnotations(
                Annotation.create(timestamp, TraceConstants.ANNO_SR,
                        Endpoint.create(apiName, ServerInfo.IP4, req.getLocalPort())));

        // app name
        apiSpan.addToBinary_annotations(BinaryAnnotation.create(
                "name", clientAutoConfigure.getName(), null
        ));

        // app owner
        apiSpan.addToBinary_annotations(BinaryAnnotation.create(
                "负责人", clientAutoConfigure.getOwner(), null
        ));
        return apiSpan;
    }

    private void endTrace(HttpServletRequest req, Span span, Stopwatch watch) {
        // ss annotation
        span.addToAnnotations(
                Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_SS,
                        Endpoint.create(span.getName(), ServerInfo.IP4, req.getLocalPort())));

        span.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));

        // send trace spans
        agent.send(TraceContext.getSpans());
    }

    @Override
    public void destroy() {
        // clear trace context
        TraceContext.clear();
    }

}