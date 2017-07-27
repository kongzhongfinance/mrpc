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

        this.endTrace(consumeSpan, watch);
        return result;
    }

    private Span startTrace(RpcRequest request) {

        // start consume span
        Span consumeSpan = new Span();
        consumeSpan.setId(Ids.get());

        long traceId  = TraceContext.getTraceId();
        long parentId = TraceContext.getSpanId();

        consumeSpan.setTrace_id(traceId);
        consumeSpan.setParent_id(parentId);

        String serviceName = request.getClassName() + "." + request.getMethodName();
        consumeSpan.setName(serviceName);

        long timestamp = TimeUtils.currentMicros();
        consumeSpan.setTimestamp(timestamp);

        // cs annotation
        int providerHost = NetUtils.ip2Num(request.getContext().get(Const.SERVER_HOST));
        int providerPort = Integer.parseInt(request.getContext().get(Const.SERVER_PORT));

        consumeSpan.addToAnnotations(
                Annotation.create(timestamp, TraceConstants.ANNO_CS,
                        Endpoint.create(serviceName, providerHost, providerPort)));

//        String providerOwner = provider.getParameter("owner");
//        if (!Strings.isNullOrEmpty(providerOwner)) {
//            // app owner
//            consumeSpan.addToBinary_annotations(BinaryAnnotation.create(
//                    "owner", providerOwner, null
//            ));
//        }
        return consumeSpan;
    }

    private void endTrace(Span consumeSpan, Stopwatch watch) {
        consumeSpan.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));

        String host = RpcContext.getAttachments(Const.SERVER_HOST);
        int    port = Integer.parseInt(RpcContext.getAttachments(Const.SERVER_PORT));

        // cr annotation
        consumeSpan.addToAnnotations(
                Annotation.create(TimeUtils.currentMicros(), TraceConstants.ANNO_CR,
                        Endpoint.create(consumeSpan.getName(), NetUtils.ip2Num(host), port)));

        String exception = RpcContext.getAttachments(Const.SERVER_EXCEPTION);
        if (StringUtils.isNotEmpty(exception)) {
            // attach exception
            consumeSpan.addToBinary_annotations(BinaryAnnotation.create(
                    "Exception", exception, null
            ));
        }

        // collect the span
        TraceContext.addSpan(consumeSpan);
    }

}
