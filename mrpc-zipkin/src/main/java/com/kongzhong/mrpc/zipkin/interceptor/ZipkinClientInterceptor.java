package com.kongzhong.mrpc.zipkin.interceptor;

import com.github.kristofa.brave.*;
import com.github.kristofa.brave.internal.Util;
import com.kongzhong.mrpc.client.invoke.ClientInvocation;
import com.kongzhong.mrpc.interceptor.RpcClientInterceptor;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.twitter.zipkin.gen.Span;
import zipkin.internal.Nullable;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author biezhi
 * @date 2017/11/22
 */
public class ZipkinClientInterceptor implements RpcClientInterceptor {

    private final ClientRequestInterceptor  clientRequestInterceptor;
    private final ClientResponseInterceptor clientResponseInterceptor;
    private final ClientSpanThreadBinder    clientSpanThreadBinder;

    public ZipkinClientInterceptor() {
        String         sendUrl     = "http://127.0.0.1:9411/api/v1/spans";
        Sender         sender      = OkHttpSender.create(sendUrl);
        AsyncReporter<zipkin.Span> reporter = AsyncReporter.builder(sender).build();
        String         application = "demo-client";//ZipkinConfig.getProperty(ZipkinConstants.BRAVE_NAME);
        Brave          brave       = new Brave.Builder(application).reporter(reporter).build();
        this.clientRequestInterceptor = Util.checkNotNull(brave.clientRequestInterceptor(), null);
        this.clientResponseInterceptor = Util.checkNotNull(brave.clientResponseInterceptor(), null);
        this.clientSpanThreadBinder = Util.checkNotNull(brave.clientSpanThreadBinder(), null);
    }

    @Override
    public Object execute(ClientInvocation invocation) throws Exception {
        Object result = null;
        clientRequestInterceptor.handle(new GrpcClientRequestAdapter(invocation));
        Map<String, String> att               = invocation.getRpcInvoker().getRequest().getContext();
        final Span          currentClientSpan = clientSpanThreadBinder.getCurrentClientSpan();
        try {
            result = invocation.next();
            clientSpanThreadBinder.setCurrentSpan(currentClientSpan);
            clientResponseInterceptor.handle(new GrpcClientResponseAdapter(result));
        } finally {
            clientSpanThreadBinder.setCurrentSpan(null);
        }
        return result;
    }

    static final class GrpcClientRequestAdapter implements ClientRequestAdapter {
        private ClientInvocation invocation;
        public GrpcClientRequestAdapter(ClientInvocation invocation) {
            this.invocation = invocation;
        }

        @Override
        public String getSpanName() {
            String serviceName = invocation.getRpcInvoker().getRequest().getClassName();
            return serviceName;
        }

        @Override
        public void addSpanIdToRequest(@Nullable SpanId spanId) {
            Map<String,String> at = this.invocation.getRpcInvoker().getRequest().getContext();
            if (spanId == null) {
                at.put("Sampled", "0");
            } else {

                at.put("Sampled", "1");
                at.put("TraceId", spanId.traceIdString());
                at.put("SpanId", IdConversion.convertToString(spanId.spanId));

                if (spanId.nullableParentId() != null) {
                    at.put("ParentSpanId", IdConversion.convertToString(spanId.parentId));
                }
            }
        }

        @Override
        public Collection<KeyValueAnnotation> requestAnnotations() {
            Object[] parameters = invocation.getRpcInvoker().getRequest().getParameters();
            KeyValueAnnotation an = KeyValueAnnotation.create("params", JacksonSerialize.toJSONString(parameters));
            return Collections.singletonList(an);
        }

        @Override
        public com.twitter.zipkin.gen.Endpoint serverAddress() {
            return null;
        }
    }

    static final class GrpcClientResponseAdapter implements ClientResponseAdapter {

        private final Object result;

        public GrpcClientResponseAdapter(Object result) {
            this.result = result;
        }

        @Override
        public Collection<KeyValueAnnotation> responseAnnotations() {
            return Collections.<KeyValueAnnotation>emptyList();
            /*
        	return !result.hasException()
                ? Collections.<KeyValueAnnotation>emptyList()
                : Collections.singletonList(KeyValueAnnotation.create("error", result.getException().getMessage()));
                */
        }
    }

}
