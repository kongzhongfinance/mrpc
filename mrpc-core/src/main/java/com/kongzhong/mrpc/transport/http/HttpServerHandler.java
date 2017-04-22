package com.kongzhong.mrpc.transport.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Throwables;
import com.kongzhong.mrpc.enums.MediaType;
import com.kongzhong.mrpc.model.RequestBody;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcRet;
import com.kongzhong.mrpc.server.RpcServer;
import com.kongzhong.mrpc.transport.SimpleServerHandler;
import com.kongzhong.mrpc.utils.JSONUtils;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * http请求处理器
 *
 * @author biezhi
 *         2017/4/21
 */
public class HttpServerHandler extends SimpleServerHandler<FullHttpRequest> {

    public static final Logger log = LoggerFactory.getLogger(HttpServerHandler.class);

    public HttpServerHandler(Map<String, Object> handlerMap) {
        super(handlerMap);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {
        String uri = httpRequest.uri();
        HttpMethod httpMethod = httpRequest.method();
        HttpHeaders headers = httpRequest.headers();
        HttpVersion httpVersion = httpRequest.protocolVersion();

        QueryStringDecoder queryDecoder = new QueryStringDecoder(uri, CharsetUtil.UTF_8);
        String path = queryDecoder.path();

        if (!"/rpc".equals(path)) {
            this.sendError(ctx, RpcRet.error("bad request."));
            return;
        }

        ByteBuf buf = httpRequest.content();
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");

        log.debug("{}", httpVersion);
        log.info("{}\t{}", httpMethod, uri);

        if (StringUtils.isEmpty(body)) {
            this.sendError(ctx, RpcRet.notFound("body not is empty."));
            return;
        }

        log.debug("body: \n{}", body);

        RequestBody requestBody = JSON.parseObject(body, RequestBody.class);

        String serviceName = requestBody.getService();
        String methodName = requestBody.getMethod();
        String version = requestBody.getVersion();
        Map<String, Object> argJSON = requestBody.getParameters();

        if (StringUtils.isEmpty(serviceName)) {
            this.sendError(ctx, RpcRet.notFound("[service] not is null."));
            return;
        }

        if (StringUtils.isEmpty(methodName)) {
            this.sendError(ctx, RpcRet.notFound("[method] not is null."));
            return;
        }

        Object bean = handlerMap.get(serviceName);
        if (null == bean) {
            this.sendError(ctx, RpcRet.notFound("not found [" + serviceName + "] bean."));
            return;
        }

        RpcRequest rpcRequest = parseParams(ctx, requestBody, bean);

        HttpResponse response = new HttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, MediaType.JSON.toString());
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        if (HttpHeaders.isKeepAlive(httpRequest)) {
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        HttpResponseCallback responseCallback = new HttpResponseCallback(rpcRequest, response, handlerMap);
        RpcServer.submit(responseCallback, ctx);
    }

    private RpcRequest parseParams(ChannelHandlerContext ctx, RequestBody requestBody, Object bean) throws NoSuchMethodException {

        String serviceName = requestBody.getService();
        String methodName = requestBody.getMethod();

        Class<?> type = bean.getClass();
        Method method = null;
        JSONArray arrJSON = null;
        List<String> parameterTypes = requestBody.getParameterTypes();
        JSONObject argJSON = requestBody.getParameters();

        if (null != parameterTypes) {
            Class<?>[] parameterTypeArr = new Class[parameterTypes.size()];
            int pos = 0;
            for (Object parameterType : parameterTypes) {
                parameterTypeArr[pos++] = ReflectUtils.from(parameterType.toString());
            }
            method = type.getMethod(methodName, parameterTypeArr);
            arrJSON = requestBody.getParameterArray();
        } else {
            method = ReflectUtils.method(type, methodName);
        }
        if (null == method) {
            this.sendError(ctx, RpcRet.notFound("method [" + methodName + "] not found."));
            return null;
        }
        Object[] args = new Object[method.getParameterCount()];
        List<String> paramNames = ReflectUtils.getParamNames(method);
        if (null != paramNames) {
            Class<?>[] types = method.getParameterTypes();
            for (int i = 0, len = paramNames.size(); i < len; i++) {
                String paramName = paramNames.get(i);
                Class<?> paramType = types[i];

                if (paramType.isArray()) {
                    Class<?> arrayType = paramType.getComponentType();
                    JSONArray array;
                    if (null != arrJSON) {
                        array = arrJSON.getJSONArray(i);
                    } else {
                        array = argJSON.getJSONArray(paramName);
                    }
                    if (null != array) {
                        List list = array.toJavaList(arrayType);
                        args[i] = listToArray(arrayType, list);
                    } else {
                        args[i] = null;
                    }
                } else {
                    if (null != arrJSON) {
                        args[i] = arrJSON.getObject(i, paramType);
                    } else {
                        args[i] = argJSON.getObject(paramName, paramType);
                    }
                }
            }
        }

        String requestId = null != requestBody.getRequestId() ? requestBody.getRequestId() : StringUtils.getUUID();
        return getRpcRequest(requestId, serviceName, method, args);
    }

    private <A> A[] listToArray(Class<A> type, List<A> list) {
        A[] a = (A[]) Array.newInstance(type, list.size());
        return (A[]) list.toArray(a);
    }

    private RpcRequest getRpcRequest(String requestId, String serviceName, Method method, Object[] paramters) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(requestId);
        request.setClassName(serviceName);
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setReturnType(method.getReturnType());
        request.setParameters(paramters);
        return request;
    }

    /**
     * 错误处理
     *
     * @param ctx
     * @param status
     */
    private void sendError(ChannelHandlerContext ctx, RpcRet ret) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(ret.getCode()), Unpooled.copiedBuffer(JSONUtils.toJSONString(ret), CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("", cause);
        sendError(ctx, RpcRet.error(Throwables.getStackTraceAsString(cause)));
        ctx.close();
    }
}