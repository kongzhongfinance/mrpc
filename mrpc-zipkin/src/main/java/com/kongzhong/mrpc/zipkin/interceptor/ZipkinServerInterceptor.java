package com.kongzhong.mrpc.zipkin.interceptor;

import com.github.kristofa.brave.*;
import com.kongzhong.mrpc.client.invoke.ClientInvocation;
import com.kongzhong.mrpc.interceptor.RpcClientInterceptor;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import static com.github.kristofa.brave.IdConversion.convertToLong;


/**
 * @author biezhi
 * @date 2017/11/22
 */
public class ZipkinServerInterceptor implements RpcServerInterceptor {

    private final ServerRequestInterceptor  serverRequestInterceptor;
    private final ServerResponseInterceptor serverResponseInterceptor;

    public ZipkinServerInterceptor() {
        String         sendUrl     = "http://127.0.0.1:9411/api/v1/spans";
        Sender         sender      = OkHttpSender.create(sendUrl);
        Reporter<Span> reporter    = AsyncReporter.builder(sender).build();
        String         application = "demo-server";//RpcContext.getContext().getUrl().getParameter("application");
        Brave          brave       = new Brave.Builder(application).reporter(reporter).build();
        this.serverRequestInterceptor = brave.serverRequestInterceptor();
        this.serverResponseInterceptor = brave.serverResponseInterceptor();
    }

    @Override
    public Object execute(ServerInvocation invocation) throws Exception {
        serverRequestInterceptor.handle(new DrpcServerRequestAdapter(invocation));
        Object result ;
        try {
            result =  invocation.next();
            serverResponseInterceptor.handle(new GrpcServerResponseAdapter(result));
        } finally {

        }
        return result;
    }

    static final class DrpcServerRequestAdapter implements ServerRequestAdapter {
        private ServerInvocation invocation;
        DrpcServerRequestAdapter(ServerInvocation invocation) {
            this.invocation = invocation;
        }

        @Override
        public TraceData getTraceData() {
            //Random randoml = new Random();
            Map<String,String> at           = this.invocation.getRequest().getContext();
            String             sampled      = at.get("Sampled");
            String             parentSpanId = at.get("ParentSpanId");
            String             traceId      = at.get("TraceId");
            String             spanId       = at.get("SpanId");

            // Official sampled value is 1, though some old instrumentation send true
            Boolean parsedSampled = sampled != null
                    ? sampled.equals("1") || sampled.equalsIgnoreCase("true")
                    : null;

            if (traceId != null && spanId != null) {
                return TraceData.create(getSpanId(traceId, spanId, parentSpanId, parsedSampled));
            } else if (parsedSampled == null) {
                return TraceData.EMPTY;
            } else if (parsedSampled.booleanValue()) {
                // Invalid: The caller requests the trace to be sampled, but didn't pass IDs
                return TraceData.EMPTY;
            } else {
                return TraceData.NOT_SAMPLED;
            }
        }

        @Override
        public String getSpanName() {
            String serviceName = invocation.getRequest().getClassName();
            return serviceName;
        }

        @Override
        public Collection<KeyValueAnnotation> requestAnnotations() {
            SocketAddress socketAddress = null;
            if (socketAddress != null) {
                KeyValueAnnotation remoteAddrAnnotation = KeyValueAnnotation.create(
                        "DRPC_REMOTE_ADDR", socketAddress.toString());
                return Collections.singleton(remoteAddrAnnotation);
            } else {
                return Collections.emptyList();
            }
        }
    }

    static final class GrpcServerResponseAdapter implements ServerResponseAdapter {
        final Object result;
        public GrpcServerResponseAdapter(Object result) {
            this.result = result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Collection<KeyValueAnnotation> responseAnnotations() {
//            return !result.hasException()
//                    ? Collections.<KeyValueAnnotation>emptyList()
//                    : Collections.singletonList(KeyValueAnnotation.create("error", result.getException().getMessage()));
            return Collections.<KeyValueAnnotation>emptyList();
        }

    }

    static SpanId getSpanId(String traceId, String spanId, String parentSpanId, Boolean sampled) {
        return SpanId.builder()
                .traceIdHigh(traceId.length() == 32 ? convertToLong(traceId, 0) : 0)
                .traceId(convertToLong(traceId))
                .spanId(convertToLong(spanId))
                .sampled(sampled)
                .parentId(parentSpanId == null ? null : convertToLong(parentSpanId)).build();
    }

}
