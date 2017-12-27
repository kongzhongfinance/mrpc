package com.kongzhong.mrpc.trace.interceptor;

import com.google.common.base.Throwables;
import com.kongzhong.basic.zipkin.TraceConstants;
import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.basic.zipkin.agent.InitializeAgent;
import com.kongzhong.basic.zipkin.util.AppConfiguration;
import com.kongzhong.basic.zipkin.util.ServerInfo;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.trace.config.TraceAutoConfigure;
import com.kongzhong.mrpc.trace.utils.ServletPathMatcher;
import com.kongzhong.mrpc.utils.Ids;
import com.kongzhong.mrpc.utils.TimeUtils;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * @author biezhi
 * @date 2017/12/1
 */
@Slf4j
public class BaseFilter {

    private AbstractAgent agent;
    private TraceAutoConfigure clientAutoConfigure;
    private Set<String> excludesPattern;
    private boolean agentInited;

    /**
     * PatternMatcher used in determining which paths to react to for a given request.
     */
    private ServletPathMatcher pathMatcher = ServletPathMatcher.getInstance();

    public BaseFilter(TraceAutoConfigure clientAutoConfigure) {
        this.clientAutoConfigure = clientAutoConfigure;
        AbstractAgent agent = InitializeAgent.getAgent();
        if (null == agent) {
            this.agent = InitializeAgent.initAndGetAgent(clientAutoConfigure.getUrl(), clientAutoConfigure.getTopic());
        } else {
            this.agent = agent;
        }

        this.agentInited = this.agent != null;
    }

    void setExcludesPattern(Set<String> excludesPattern) {
        this.excludesPattern = excludesPattern;
    }

    boolean enabled() {
        return clientAutoConfigure.getEnable();
    }

    public void startTrace(HttpServletRequest request) {
        try {
            String uri = request.getRequestURI();
            // start root span
            Span rootSpan = startTrace(request, uri);
            TraceContext.setRootSpan(rootSpan);
            if (log.isDebugEnabled()) {
                log.debug("Trace request url: {}", uri);
                TraceContext.print();
            }
            // prepare trace context
            TraceContext.addSpanAndUpdate(rootSpan);
        } catch (Exception e) {
            log.error("startTrace error ", e);
        }
    }

    private Span startTrace(HttpServletRequest req, String point) {
        Span apiSpan = new Span();

        // span basic data
        long timestamp = TimeUtils.currentMicros();

        long id = Ids.get();
        apiSpan.setId(id);
        apiSpan.setTrace_id(id);
        apiSpan.setName(point);
        apiSpan.setTimestamp(timestamp);

        // sr annotation
        apiSpan.addToAnnotations(
                Annotation.create(timestamp, TraceConstants.ANNO_SR,
                        Endpoint.create(AppConfiguration.getAppId(), ServerInfo.IP4, req.getLocalPort())));

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

    public void endTrace(HttpServletRequest request) {
        endTrace(request, null);
    }

    public void endTrace(HttpServletRequest request, Throwable throwable) {
        try {
            // end root span
            Span rootSpan = TraceContext.getRootSpan();
            if (null != rootSpan) {
                long times = TimeUtils.currentMicros() - rootSpan.getTimestamp();
                endTrace(request, rootSpan, times, throwable);
            }
        } catch (Exception e) {
            log.error("endTrace error ", e);
        } finally {
            // clear trace context
            TraceContext.clear();
            if (log.isDebugEnabled()) {
                log.debug("Filter Trace clear. traceId={}", TraceContext.getTraceId());
                TraceContext.print();
            }
        }
    }

    private void endTrace(HttpServletRequest req, Span span, long times, Throwable throwable) {
        // ss annotation
        span.addToAnnotations(
                Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_SS,
                        Endpoint.create(AppConfiguration.getAppId(), ServerInfo.IP4, req.getLocalPort())));

        span.setDuration(times);

        if (null != throwable) {
            // attach exception
            span.addToBinary_annotations(BinaryAnnotation.create(
                    "Exception", Throwables.getStackTraceAsString(throwable), null));
        }

        TraceContext.addSpanAndUpdate(span);

        if (!this.agentInited) {
            return;
        }
        // send trace spans
        try {
            List<Span> spans = TraceContext.getSpans();
            agent.send(spans);
            if (log.isDebugEnabled()) {
                log.debug("Filter Send trace data {}.", JacksonSerialize.toJSONString(TraceContext.getSpans()));
            }
        } catch (Exception e) {
            log.error("Filter 发送到Trace失败", e);
        }
    }

    boolean isExclusion(HttpServletRequest request) {
        String contextPath = getContextPath(request);
        String requestURI = request.getRequestURI();
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

    private String getContextPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        if (contextPath == null || contextPath.length() == 0) {
            contextPath = "/";
        }
        return contextPath;
    }

}
