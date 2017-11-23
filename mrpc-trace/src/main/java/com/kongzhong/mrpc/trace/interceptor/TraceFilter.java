package com.kongzhong.mrpc.trace.interceptor;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.kongzhong.mrpc.trace.TraceAgent;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.trace.TraceContext;
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

    private TraceClientAutoConfigure conf;
    private TraceAgent               agent;

    public TraceFilter(TraceClientAutoConfigure conf) {
        this.conf = conf;
        this.agent = new TraceAgent(conf.getUrl());
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        if (!conf.getEnable()) {
            return;
        }
        agent = new TraceAgent(conf.getUrl());
        log.info("init the trace interceptor with config({}).", new Object[]{config});
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!conf.getEnable()) {
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
                Annotation.create(timestamp, TraceConstants.ANNO_CS,
                        Endpoint.create(apiName, ServerInfo.IP4, req.getLocalPort())));

        // app name
        apiSpan.addToBinary_annotations(BinaryAnnotation.create(
                "name", conf.getName(), null
        ));

        // app owner
        apiSpan.addToBinary_annotations(BinaryAnnotation.create(
                "owner", conf.getOwner(), null
        ));

        // trace desc
        if (!Strings.isNullOrEmpty(point)) {
            apiSpan.addToBinary_annotations(BinaryAnnotation.create(
                    "description", "描述信息", null
            ));
        }
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