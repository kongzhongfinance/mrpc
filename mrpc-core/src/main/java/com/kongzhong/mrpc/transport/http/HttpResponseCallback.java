package com.kongzhong.mrpc.transport.http;

import com.google.common.base.Throwables;
import com.kongzhong.mrpc.exception.ServiceException;
import com.kongzhong.mrpc.model.ExceptionMeta;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.transport.SimpleResponseCallback;
import com.kongzhong.mrpc.utils.JSONUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.kongzhong.mrpc.model.Const.HEADER_REQUEST_ID;

/**
 * Http响应回调处理
 */
public class HttpResponseCallback extends SimpleResponseCallback<FullHttpResponse> {

    public static final Logger log = LoggerFactory.getLogger(HttpResponseCallback.class);

    private FullHttpResponse httpResponse;

    public HttpResponseCallback(RpcRequest request, FullHttpResponse httpResponse, Map<String, Object> handlerMap) {
        super(request, null, handlerMap);
        this.httpResponse = httpResponse;
    }

    @Override
    public FullHttpResponse call() throws Exception {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(request.getRequestId());
        try {
            Object result = handle(request);
            rpcResponse.setResult(result);
            if (null != request.getReturnType()) {
                rpcResponse.setReturnType(request.getReturnType().getName());
            }
            rpcResponse.setSuccess(true);
        } catch (Throwable e) {
            Throwable t = e instanceof ServiceException ? e.getCause() : e;
            Class<?> exceptionType = t.getClass();
            Field[] fields = exceptionType.getDeclaredFields();
            if (null != fields && fields.length > 0) {
                List<ExceptionMeta> ftypes = new ArrayList<>();
                for (Field field : fields) {
                    if ("serialVersionUID".equals(field.getName())) {
                        continue;
                    }
                    Class<?> ftype = field.getType();
                    field.setAccessible(true);
                    ExceptionMeta exceptionMeta = new ExceptionMeta(ftype.getTypeName(), field.get(t));
                    ftypes.add(exceptionMeta);
                }
                if (!ftypes.isEmpty()) {
                    rpcResponse.setResult(ftypes);
                }
            }

            String exceptionName = exceptionType.getName();
            String exception = Throwables.getStackTraceAsString(t).replace(exceptionName + ": ", "");
            exception = exception.replace(exceptionName, "");
            rpcResponse.setReturnType(exceptionName);
            rpcResponse.setException(exception);
            rpcResponse.setMessage(t.getMessage());
            rpcResponse.setSuccess(false);

            log.error("rpc method invoke error", t);
        } finally {
            RpcContext.remove();
            String body = JSONUtils.toJSONString(rpcResponse);
            ByteBuf bbuf = Unpooled.wrappedBuffer(body.getBytes(CharsetUtil.UTF_8));
            httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bbuf.readableBytes());
            httpResponse.headers().set(HEADER_REQUEST_ID, request.getRequestId());
            httpResponse.content().clear().writeBytes(bbuf);
            return httpResponse;
        }
    }

}