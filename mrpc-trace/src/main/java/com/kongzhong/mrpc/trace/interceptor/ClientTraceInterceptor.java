package com.kongzhong.mrpc.trace.interceptor;

import com.google.common.base.Stopwatch;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.client.invoke.ClientInvocation;
import com.kongzhong.mrpc.client.invoke.RpcInvoker;
import com.kongzhong.mrpc.interceptor.RpcClientInterceptor;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.trace.TraceContext;
import com.kongzhong.mrpc.utils.Ids;
import com.kongzhong.mrpc.utils.NetUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import com.kongzhong.mrpc.utils.TimeUtils;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * ClientTraceInterceptor
 */
@Slf4j
public class ClientTraceInterceptor implements RpcClientInterceptor {

    @Override
    public Object execute(ClientInvocation invocation) throws Exception {
        if (TraceContext.getTraceId() == null) {
            // not need tracing
            return invocation.next();
        }

        RpcInvoker invoker = invocation.getRpcInvoker();
        RpcRequest request = invoker.getRequest();

        // start the watch
        Stopwatch watch       = Stopwatch.createStarted();
        Span      consumeSpan = this.startTrace(request);

        log.debug("consumer invoke before: ");
        TraceContext.print();

        Map<String, String> context = request.getContext();

        context.put(TraceConstants.TRACE_ID, consumeSpan.getTrace_id() + "");
        context.put(TraceConstants.SPAN_ID, consumeSpan.getId() + "");

        Object result = invoker.invoke();

        log.debug("consumer invoke after: ");
        TraceContext.print();

        log.debug("sr time: " + RpcContext.getAttachments(TraceConstants.SR_TIME));
        log.debug("ss time: " + RpcContext.getAttachments(TraceConstants.SS_TIME));

        this.endTrace(request, consumeSpan, watch);
        return result;
    }

    private Span startTrace(RpcRequest request) {

        // start client span
        Span clientSpan = new Span();
        clientSpan.setId(Ids.get());

        long traceId  = TraceContext.getTraceId();
        long parentId = TraceContext.getSpanId();

        clientSpan.setTrace_id(traceId);
        clientSpan.setParent_id(parentId);
        clientSpan.setName(request.getMethodName());

        long timestamp = TimeUtils.currentMicros();
        clientSpan.setTimestamp(timestamp);

        // cs annotation
        int providerHost = NetUtils.ip2Num(request.getContext().get(Const.SERVER_HOST));
        int providerPort = Integer.parseInt(request.getContext().get(Const.SERVER_PORT));

        clientSpan.addToAnnotations(
                Annotation.create(timestamp, TraceConstants.ANNO_CS,
                        Endpoint.create(request.getContext().getOrDefault(Const.SERVER_NAME, request.getClassName()), providerHost, providerPort)));

        String owners = request.getContext().get(Const.SERVER_OWNER);
        if (StringUtils.isNotEmpty(owners)) {
            Stream.of(owners.split(","))
                .forEach(owner -> {
                    // app owner
                    clientSpan.addToBinary_annotations(BinaryAnnotation.create(
                            "负责人", owner, null
                    ));
                });
        }
        String emails = request.getContext().get(Const.SERVER_OWNER_EMAIL);
        if (StringUtils.isNotEmpty(emails)) {
            Stream.of(emails.split(","))
                    .forEach(email -> {
                        // app owner
                        clientSpan.addToBinary_annotations(BinaryAnnotation.create(
                                "负责人邮箱", email, null
                        ));
                    });
        }
        return clientSpan;
    }

    private void endTrace(RpcRequest request, Span clientSpan, Stopwatch watch) {
        clientSpan.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));

        String host = RpcContext.getAttachments(Const.SERVER_HOST);
        int    port = Integer.parseInt(RpcContext.getAttachments(Const.SERVER_PORT));

        // cr annotation
        clientSpan.addToAnnotations(
                Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_CR,
                        Endpoint.create(request.getMethodName(), NetUtils.ip2Num(host), port)));

        String exception = RpcContext.getAttachments(Const.SERVER_EXCEPTION);
        if (StringUtils.isNotEmpty(exception)) {
            // attach exception
            clientSpan.addToBinary_annotations(BinaryAnnotation.create(
                    "Exception", exception, null));
        }

        // collect the span
        TraceContext.addSpan(clientSpan);
    }

}
