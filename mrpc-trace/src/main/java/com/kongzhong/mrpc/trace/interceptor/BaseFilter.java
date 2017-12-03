package com.kongzhong.mrpc.trace.interceptor;

import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.basic.zipkin.agent.KafkaAgent;
import com.kongzhong.basic.zipkin.util.AppConfiguration;
import com.kongzhong.basic.zipkin.util.NetUtils;
import com.kongzhong.basic.zipkin.util.ServerInfo;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.trace.config.TraceClientAutoConfigure;
import com.kongzhong.mrpc.utils.Ids;
import com.kongzhong.mrpc.utils.TimeUtils;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author biezhi
 * @date 2017/12/1
 */
@Slf4j
public class BaseFilter {

    private AbstractAgent            agent;
    private TraceClientAutoConfigure clientAutoConfigure;

    public BaseFilter(TraceClientAutoConfigure clientAutoConfigure) {
        try {
            this.clientAutoConfigure = clientAutoConfigure;
            this.agent = new KafkaAgent(clientAutoConfigure.getUrl(), clientAutoConfigure.getTopic());
        } catch (Exception e) {
            log.error("初始化Trace客户端失败", e);
        }
    }

    public boolean enabled() {
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
        }catch (Exception e){
            log.error("startTrace error ", e);
        }
    }

    private Span startTrace(HttpServletRequest req, String point) {
        Span   apiSpan = new Span();

        // span basic data
        long timestamp = TimeUtils.currentMicros();

        long id = Ids.get();
        apiSpan.setId(id);
        apiSpan.setTrace_id(id);
        apiSpan.setName(point);
        apiSpan.setTimestamp(timestamp);

        // cs annotation
        apiSpan.addToAnnotations(
                Annotation.create(timestamp, TraceConstants.ANNO_CS,
                        Endpoint.create(AppConfiguration.getAppId(), NetUtils.ip2Num(req.getRemoteHost()), req.getLocalPort())));

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
        try {

            // end root span
            Span rootSpan = TraceContext.getRootSpan();
            if (null != rootSpan) {
                long times = TimeUtils.currentMicros() - rootSpan.getTimestamp();
                endTrace(request, rootSpan, times);
            }
            // clear trace context
            TraceContext.clear();
        }catch (Exception e){
            log.error("endTrace error ", e);
        }
    }

    private void endTrace(HttpServletRequest req, Span span, long times) {
        // ss annotation
        span.addToAnnotations(
                Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_SS,
                        Endpoint.create(AppConfiguration.getAppId(), ServerInfo.IP4, req.getLocalPort())));

        // cr annotation
        span.addToAnnotations(
                Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_CR,
                        Endpoint.create(AppConfiguration.getAppId(), NetUtils.ip2Num(req.getRemoteHost()), req.getLocalPort())));


        span.setDuration(times);

        TraceContext.addSpanAndUpdate(span);
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

        if (log.isDebugEnabled()) {
            log.debug("Filter Trace clear. traceId={}", TraceContext.getTraceId());
            TraceContext.print();
        }
    }

}
