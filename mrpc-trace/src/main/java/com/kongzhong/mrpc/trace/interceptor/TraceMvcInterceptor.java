package com.kongzhong.mrpc.trace.interceptor;

import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.basic.zipkin.agent.KafkaAgent;
import com.kongzhong.basic.zipkin.util.ServerInfo;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.trace.config.TraceClientAutoConfigure;
import com.kongzhong.mrpc.trace.utils.Exclusions;
import com.kongzhong.mrpc.trace.utils.ServletPathMatcher;
import com.kongzhong.mrpc.utils.Ids;
import com.kongzhong.mrpc.utils.TimeUtils;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * @author biezhi
 * @date 2017/11/29
 */
@Data
@Slf4j
public class TraceMvcInterceptor extends HandlerInterceptorAdapter {

    private TraceClientAutoConfigure clientAutoConfigure;
    private AbstractAgent            agent;
    private Set<String>        excludesPattern = Exclusions.defaultExclusions().getExclusions();
    private ServletPathMatcher pathMatcher     = ServletPathMatcher.getInstance();

    public TraceMvcInterceptor(TraceClientAutoConfigure clientAutoConfigure) {
        this.clientAutoConfigure = clientAutoConfigure;
        this.agent = new KafkaAgent(clientAutoConfigure.getUrl(), clientAutoConfigure.getTopic());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!clientAutoConfigure.getEnable()) {
            return true;
        }

        String uri = request.getRequestURI();

        if (isExclusion(request.getContextPath(), uri)) {
            return true;
        }


        // start root span
        Span rootSpan = startTrace(request, uri);
        TraceContext.setRootSpan(rootSpan);
        if (log.isDebugEnabled()) {
            log.debug("Trace request url: {}", uri);
            log.debug("Current thread: [{}], trace context: traceId={}, spanId={}", Thread.currentThread().getName(), Long.toHexString(TraceContext.getTraceId()), Long.toHexString(TraceContext.getSpanId()));
        }
        // prepare trace context
        TraceContext.start();
        TraceContext.setTraceId(rootSpan.getTrace_id());
        TraceContext.setSpanId(rootSpan.getId());
        TraceContext.addSpan(rootSpan);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
        // end root span
        Span rootSpan = TraceContext.getRootSpan();
        if (null != rootSpan) {
            long times = TimeUtils.currentMicros() - rootSpan.getTimestamp();
            endTrace(request, rootSpan, times);
        }
    }

    private Span startTrace(HttpServletRequest req, String point) {

        String apiName = req.getRequestURI();
        Span   apiSpan = new Span();

        // span basic data
        Long traceId = TraceContext.getTraceId();
        if (null == traceId) {
            traceId = Ids.get();
        }

        long timestamp = TimeUtils.currentMicros();

        apiSpan.setId(traceId);
        apiSpan.setTrace_id(traceId);
        apiSpan.setName(point);
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

    private boolean isExclusion(String contextPath, String requestURI) {
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

    private void endTrace(HttpServletRequest req, Span span, long times) {
        // ss annotation
        span.addToAnnotations(
                Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_SS,
                        Endpoint.create(span.getName(), ServerInfo.IP4, req.getLocalPort())));

        span.setDuration(times);

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
        if (log.isDebugEnabled()) {
            log.debug("Trace clear.");
            TraceContext.print();
        }
    }

}
