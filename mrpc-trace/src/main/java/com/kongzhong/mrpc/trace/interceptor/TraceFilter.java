package com.kongzhong.mrpc.trace.interceptor;

import com.google.common.base.Stopwatch;
import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.basic.zipkin.util.ServerInfo;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.trace.config.TraceClientAutoConfigure;
import com.kongzhong.mrpc.trace.utils.ServletPathMatcher;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * TraceFilter
 */
@Slf4j
public class TraceFilter implements Filter {

    private static final String PARAM_NAME_EXCLUSIONS = "exclusions";

    private TraceClientAutoConfigure clientAutoConfigure;
    private AbstractAgent            agent;
    private Set<String>              excludesPattern;
    private String                   contextPath;

    /**
     * PatternMatcher used in determining which paths to react to for a given request.
     */
    private ServletPathMatcher pathMatcher = ServletPathMatcher.getInstance();

    public TraceFilter(TraceClientAutoConfigure clientAutoConfigure, AbstractAgent agent) {
        this.clientAutoConfigure = clientAutoConfigure;
        this.agent = agent;
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        String exclusions = config.getInitParameter(PARAM_NAME_EXCLUSIONS);
        if (exclusions != null && exclusions.trim().length() != 0) {
            excludesPattern = new HashSet<>(Arrays.asList(exclusions.split("\\s*,\\s*")));
        }
        this.contextPath = getContextPath(config.getServletContext());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!clientAutoConfigure.getEnable()) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        String             uri = req.getRequestURI();

        if (isExclusion(uri)) {
            chain.doFilter(request, response);
            return;
        }


        // do trace
        Stopwatch watch = Stopwatch.createStarted();

        // start root span
        Span rootSpan = startTrace(req, uri);

        if (log.isDebugEnabled()) {
            log.debug("Trace filter url: {}", uri);
            log.debug("Current thread: [{}], trace context: traceId={}, spanId={}", Thread.currentThread().getName(), Long.toHexString(TraceContext.getTraceId()), Long.toHexString(TraceContext.getSpanId()));
        }

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
        Long traceId = TraceContext.getTraceId();
        if (null == traceId) {
            traceId = Ids.get();
        }

        apiSpan.setId(traceId);
        apiSpan.setTrace_id(traceId);
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

    public boolean isExclusion(String requestURI) {
        if (excludesPattern == null || requestURI == null) {
            return false;
        }

        if (contextPath != null && requestURI.startsWith(contextPath)) {
            requestURI = requestURI.substring(contextPath.length());
            if (!requestURI.startsWith("/")) {
                requestURI = "/" + requestURI;
            }
        }

        for (String pattern : excludesPattern) {
            if (pathMatcher.matches(pattern, requestURI)) {
                return true;
            }
        }

        return false;
    }

    private void endTrace(HttpServletRequest req, Span span, Stopwatch watch) {
        // ss annotation
        span.addToAnnotations(
                Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_SS,
                        Endpoint.create(span.getName(), ServerInfo.IP4, req.getLocalPort())));

        span.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));

        // send trace spans
        try {
            agent.send(TraceContext.getSpans());
            if (log.isDebugEnabled()) {
                log.debug("Send trace data {}.", JacksonSerialize.toJSONString(TraceContext.getSpans()));
            }
        } catch (Exception e) {
            log.error("发送到Trace失败", e);
        }
        // clear trace context
        TraceContext.clear();

    }

    @Override
    public void destroy() {
        // clear trace context
        TraceContext.clear();
    }

    private static String getContextPath_2_5(ServletContext context) {
        String contextPath = context.getContextPath();

        if (contextPath == null || contextPath.length() == 0) {
            contextPath = "/";
        }

        return contextPath;
    }

    private static String getContextPath(ServletContext context) {
        if (context.getMajorVersion() == 2 && context.getMinorVersion() < 5) {
            return null;
        }
        try {
            return getContextPath_2_5(context);
        } catch (NoSuchMethodError error) {
            return null;
        }
    }

}