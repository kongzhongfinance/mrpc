package com.kongzhong.mrpc.trace.interceptor;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.kongzhong.basic.zipkin.TraceConstants;
import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.basic.zipkin.agent.InitializeAgent;
import com.kongzhong.basic.zipkin.agent.KafkaAgent;
import com.kongzhong.basic.zipkin.util.AppConfiguration;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.client.invoke.ClientInvocation;
import com.kongzhong.mrpc.client.invoke.RpcInvoker;
import com.kongzhong.mrpc.interceptor.RpcClientInterceptor;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.trace.config.TraceClientAutoConfigure;
import com.kongzhong.mrpc.trace.utils.RequestUtils;
import com.kongzhong.mrpc.utils.Ids;
import com.kongzhong.mrpc.utils.NetUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import com.kongzhong.mrpc.utils.TimeUtils;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ClientTraceInterceptor
 */
@Slf4j
@Data
public class TraceClientInterceptor implements RpcClientInterceptor {

    private AbstractAgent agent;

    private TraceClientAutoConfigure traceClientAutoConfigure;

    public TraceClientInterceptor(TraceClientAutoConfigure traceClientAutoConfigure) {
        if (null == traceClientAutoConfigure) {
            this.traceClientAutoConfigure = new TraceClientAutoConfigure();
        } else {
            this.traceClientAutoConfigure = traceClientAutoConfigure;
            AbstractAgent agent = InitializeAgent.getAgent();
            if (null == agent) {
                this.agent = InitializeAgent.initAndGetAgent(traceClientAutoConfigure.getUrl(), traceClientAutoConfigure.getTopic());
            } else {
                this.agent = agent;
            }
        }
    }

    @Override
    public Object execute(ClientInvocation invocation) throws Throwable {

        if (!traceClientAutoConfigure.getEnable()) {
            // not need tracing
            return invocation.next();
        }

        RpcInvoker invoker = invocation.getRpcInvoker();
        RpcRequest request = invoker.getRequest();

        // start the watch
        Stopwatch watch = Stopwatch.createStarted();

        // start the watch
        Span consumeSpan = this.startTrace(request);

        log.debug("consumer invoke before: ");
        TraceContext.print();

        try {
            Object result = invoker.invoke();

            if (log.isDebugEnabled()) {
                log.debug("consumer invoke after: ");
                TraceContext.print();
                log.debug("consumeSpan={} sr time: {} , ss time: {}", consumeSpan == null ? null : Long.toHexString(consumeSpan.getId()), RpcContext.getAttachments(TraceConstants.SR_TIME), RpcContext.getAttachments(TraceConstants.SS_TIME));
            }

            this.endTrace(request, consumeSpan, watch, null);
            return result;
        } catch (Exception e) {
            this.endTrace(request, consumeSpan, watch, e);
            throw e;
        }
    }

    private Span startTrace(RpcRequest request) {
        try {
            // start client span
            Span clientSpan = new Span();

            Long traceId = TraceContext.getTraceId();
            Long parentId;
            if (null == traceId) {
                traceId = Ids.get();
                parentId = traceId;
            } else {
                parentId = TraceContext.getSpanId();
            }

            clientSpan.setId(Ids.get());
            clientSpan.setTrace_id(traceId);
            clientSpan.setParent_id(parentId);
            clientSpan.setName(RequestUtils.getServerName(request.getClassName(), request.getMethodName()));

            long timestamp = TimeUtils.currentMicros();
            clientSpan.setTimestamp(timestamp);

            // cs annotation
            int providerHost = NetUtils.ip2Num(request.getContext().get(Const.SERVER_HOST));
            int providerPort = Integer.parseInt(request.getContext().get(Const.SERVER_PORT));

            clientSpan.addToAnnotations(
                    Annotation.create(timestamp, TraceConstants.ANNO_CS,
                            Endpoint.create(AppConfiguration.getAppId(), providerHost, providerPort)));

            String owners = request.getContext().get(Const.SERVER_OWNER);
            if (StringUtils.isNotEmpty(owners)) {
                // app owner
                clientSpan.addToBinary_annotations(BinaryAnnotation.create(
                        "owner", owners, null
                ));
            }

            // attach trace data
            request.addContext(TraceConstants.TRACE_ID, String.valueOf(clientSpan.getTrace_id()));
            request.addContext(TraceConstants.SPAN_ID, String.valueOf(clientSpan.getId()));

            TraceContext.addSpanAndUpdate(clientSpan);
            return clientSpan;

        } catch (Exception e) {
            log.error("startTrace error ", e);
        }
        return null;
    }

    private void endTrace(RpcRequest request, Span clientSpan, Stopwatch watch, Exception e) {
        try {
            if (clientSpan == null) {
                return;
            }
            clientSpan.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));

            String host = RpcContext.getAttachments(Const.SERVER_HOST);
            int    port = Integer.parseInt(RpcContext.getAttachments(Const.SERVER_PORT));

            // cr annotation
            clientSpan.addToAnnotations(
                    Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_CR,
                            Endpoint.create(AppConfiguration.getAppId(), NetUtils.ip2Num(host), port)));

            if (null != e) {
                // attach exception
                clientSpan.addToBinary_annotations(BinaryAnnotation.create(
                        "Exception", Throwables.getStackTraceAsString(e), null));
            }

            List<Span> spans = TraceContext.getSpans();
            agent.send(spans);
            if (log.isDebugEnabled()) {
                log.debug("Client Send trace data {}.", JacksonSerialize.toJSONString(spans));
            }
        } catch (Exception e1) {
            log.error("endTrace error ", e1);
        }
    }
}
