package com.kongzhong.mrpc.transport.http;

import com.google.common.base.Throwables;
import com.kongzhong.mrpc.enums.MediaTypeEnum;
import com.kongzhong.mrpc.exception.SerializeException;
import com.kongzhong.mrpc.model.*;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.server.SimpleRpcServer;
import com.kongzhong.mrpc.transport.netty.SimpleServerHandler;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static com.kongzhong.mrpc.Const.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * http请求处理器
 *
 * @author biezhi
 *         2017/4/21
 */
@Slf4j
public class HttpServerHandler extends SimpleServerHandler<FullHttpRequest> {

    HttpServerHandler() {
        super();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {

        super.channelRead0(ctx, httpRequest);

        String             uri          = httpRequest.uri();
        QueryStringDecoder queryDecoder = new QueryStringDecoder(uri, CharsetUtil.UTF_8);
        String             path         = queryDecoder.path();

        if ("/status".equals(path)) {
            log.debug("Rpc receive ping for {}", ctx.channel());

            String           address       = ctx.channel().localAddress().toString();
            ServiceStatus    serviceStatus = ServiceStatusTable.me().getServiceStatus(address.substring(1));
            FullHttpResponse httpResponse;
            if (null != serviceStatus) {
                httpResponse = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(JacksonSerialize.toJSONString(serviceStatus), CharsetUtil.UTF_8));
            } else {
                httpResponse = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_GATEWAY, Unpooled.copiedBuffer("", CharsetUtil.UTF_8));
            }
            httpResponse.headers().set(CONTENT_LENGTH, httpResponse.content().readableBytes());
            ctx.write(httpResponse);
            return;
        }

        if (!"/rpc".equals(path)) {
            this.sendError(ctx, RpcRet.error("bad request."));
            return;
        }

        String body = httpRequest.content().toString(CharsetUtil.UTF_8);
        if (StringUtils.isEmpty(body)) {
            this.sendError(ctx, RpcRet.notFound("body not is empty."));
            return;
        }

        RequestBody requestBody;
        try {
            requestBody = JacksonSerialize.parseObject(body, RequestBody.class);
            log.debug("Server receive body: \n{}", JacksonSerialize.toJSONString(requestBody, true));
        } catch (Exception e) {
            log.error("Server receive body parse error", e);
            this.sendError(ctx, RpcRet.error("Unable to identify the requested format."));
            return;
        }

        String serviceName = requestBody.getService();
        String methodName  = requestBody.getMethod();

        if (StringUtils.isEmpty(serviceName)) {
            this.sendError(ctx, RpcRet.notFound("[service] not is null."));
            return;
        }

        if (StringUtils.isEmpty(methodName)) {
            this.sendError(ctx, RpcRet.notFound("[method] not is null."));
            return;
        }

        ServiceBean serviceBean = serviceBeanMap.get(serviceName);
        if (null == serviceBean) {
            this.sendError(ctx, RpcRet.notFound("Not register service [" + serviceName + "]."));
            return;
        }

        Object bean = serviceBean.getBean();
        if (null == bean) {
            this.sendError(ctx, RpcRet.notFound("Not found bean [" + serviceName + "]."));
            return;
        }

        // 解析请求
        RpcRequest rpcRequest = this.parseParams(ctx, requestBody, bean.getClass());

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("", CharsetUtil.UTF_8));
        httpResponse.headers().set(CONTENT_TYPE, MediaTypeEnum.JSON.toString());
        httpResponse.headers().set(HEADER_REQUEST_ID, rpcRequest.getRequestId());
        httpResponse.headers().set(HEADER_SERVICE_CLASS, rpcRequest.getClassName());
        httpResponse.headers().set(HEADER_METHOD_NAME, rpcRequest.getMethodName());
        httpResponse.headers().set(CONTENT_LENGTH, httpResponse.content().readableBytes());
        httpResponse.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        httpResponse.headers().set(CACHE_CONTROL, "no-cache");
        httpResponse.headers().set(PRAGMA, "no-cache");
        httpResponse.headers().set(EXPIRES, "-1");

        if (HttpUtil.isKeepAlive(httpRequest)) {
            httpResponse.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        HttpResponseInvoker responseCallback = new HttpResponseInvoker(rpcRequest, httpResponse, serviceBeanMap);
        SimpleRpcServer.submit(responseCallback, ctx);
    }

    /**
     * 解析请求参数
     *
     * @param ctx         NettyChannel上下文
     * @param requestBody 请求体
     * @param type        方法所在Class
     * @return 返回一个RpcRequest
     * @throws NoSuchMethodException 当方法签名不存在时抛出
     */
    private RpcRequest parseParams(ChannelHandlerContext ctx, RequestBody requestBody, Class<?> type) throws Exception {

        String serviceName = requestBody.getService();
        String methodName  = requestBody.getMethod();

        Method       method  = ReflectUtils.method(type, methodName);
        List<Object> argJSON = requestBody.getParameters();

        // 找不到method
        if (null == method) {
            this.sendError(ctx, RpcRet.notFound("method [" + methodName + "] not found."));
            return null;
        }

        // 解析参数到args中
        Object[] args                  = new Object[method.getParameterCount()];
        Type[]   genericParameterTypes = method.getGenericParameterTypes();

        for (int i = 0; i < args.length; i++) {
            Type paramType = genericParameterTypes[i];
            args[i] = JacksonSerialize.parseObject(JacksonSerialize.toJSONString(argJSON.get(i)), paramType);
        }

        // 构造请求
        String requestId = null != requestBody.getRequestId() ? requestBody.getRequestId() : StringUtils.getUUID();

        return RpcRequest.builder()
                .requestId(requestId)
                .className(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .returnType(method.getReturnType())
                .parameters(args)
                .build();
    }

    /**
     * 错误处理
     *
     * @param ctx NettyChannel上下文
     * @param ret Rpc Http响应
     */
    private void sendError(ChannelHandlerContext ctx, RpcRet ret) throws SerializeException {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(ret.getCode()),
                Unpooled.copiedBuffer(JacksonSerialize.toJSONString(ret), CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        ctx.write(response)/*.addListener(ChannelFutureListener.CLOSE)*/;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Server receive body error", cause);
        RpcRet           ret      = RpcRet.error(Throwables.getStackTraceAsString(cause));
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(ret.getCode()), Unpooled.copiedBuffer(JacksonSerialize.toJSONString(ret), CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        ctx.writeAndFlush(response)/*.addListener(ChannelFutureListener.CLOSE)*/;

    }
}