package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.enums.MediaTypeEnum;
import com.kongzhong.mrpc.exception.ConnectException;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.exception.SerializeException;
import com.kongzhong.mrpc.model.*;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.server.SimpleRpcServer;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.transport.netty.SimpleServerHandler;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

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
 * 2017/4/21
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
                ByteBuf byteBuf = Unpooled.copiedBuffer(JacksonSerialize.toJSONString(serviceStatus), CharsetUtil.UTF_8);
                httpResponse = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, byteBuf);
            } else {
                ByteBuf byteBuf = Unpooled.copiedBuffer("", CharsetUtil.UTF_8);
                httpResponse = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_GATEWAY, byteBuf);
            }
            httpResponse.headers().set(CONTENT_LENGTH, httpResponse.content().readableBytes());
            ctx.write(httpResponse);
            return;
        }

        if (!"/rpc".equals(path)) {
            log.warn("Client {} request [{}]", ctx.channel(), path);
            this.sendError(ctx, httpRequest, new RpcException("Bad request"));
            return;
        }

        if (isShutdown) {
            this.sendError(ctx, httpRequest, new ConnectException("The server has been shutdown."));
            return;
        }

        String body = httpRequest.content().toString(CharsetUtil.UTF_8);
        if (StringUtils.isEmpty(body)) {
            this.sendError(ctx, httpRequest, new RpcException("Request body not is empty."));
            return;
        }

        RequestBody requestBody;
        try {
            requestBody = JacksonSerialize.parseObject(body, RequestBody.class);

            // TODO: 兼容期，过后删除
            if(null != requestBody.getContext()){
                MDC.put(TraceConstants.TRACE_ID, requestBody.getContext().get(TraceConstants.TRACE_ID));
            }

            log.debug("Server receive body: {}", JacksonSerialize.toJSONString(requestBody));
        } catch (Exception e) {
            log.error("Server receive body parse error", e);
            this.sendError(ctx, httpRequest, new RpcException("Unable to identify the request format."));
            return;
        }

        String serviceName = requestBody.getService();
        String methodName  = requestBody.getMethod();

        if (StringUtils.isEmpty(serviceName)) {
            this.sendError(ctx, httpRequest, new RpcException("Service not is null."));
            return;
        }

        if (StringUtils.isEmpty(methodName)) {
            this.sendError(ctx, httpRequest, new RpcException("Method not is null."));
            return;
        }

        ServiceBean serviceBean = serviceBeanMap.get(serviceName);
        if (null == serviceBean) {
            this.sendError(ctx, httpRequest, new RpcException("Not register service [" + serviceName + "]."));
            return;
        }

        Object bean = serviceBean.getBean();
        if (null == bean) {
            this.sendError(ctx, httpRequest, new RpcException("Not found bean [" + serviceName + "]."));
            return;
        }

        // 解析请求
        RpcRequest rpcRequest = this.parseParams(ctx, httpRequest, requestBody, bean.getClass());
        if (null != rpcRequest) {
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
    private RpcRequest parseParams(ChannelHandlerContext ctx, FullHttpRequest httpRequest, RequestBody requestBody, Class<?> type) throws Exception {

        String serviceName = requestBody.getService();
        String methodName  = requestBody.getMethod();

        Method       method  = ReflectUtils.method(type, methodName);
        List<Object> argJSON = requestBody.getParameters();

        // 找不到method
        if (null == method) {
            this.sendError(ctx, httpRequest, new RpcException("Method [" + methodName + "] not found."));
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
                .context(requestBody.getContext())
                .parameterTypes(method.getParameterTypes())
                .returnType(method.getReturnType())
                .parameters(args)
                .build();
    }

    /**
     * 错误处理
     *
     * @param ctx NettyChannel上下文
     */
    private void sendError(ChannelHandlerContext ctx, FullHttpRequest msg, Exception e) throws SerializeException {
        RpcResponse rpcResponse = new RpcResponse();
        if (null != msg) {
            rpcResponse.setRequestId(msg.headers().get(Const.HEADER_REQUEST_ID, ""));
        }
        rpcResponse.setSuccess(false);
        rpcResponse.setException(JacksonSerialize.toJSONString(e));
        rpcResponse.setReturnType(ConnectException.class.getName());

        String body = JacksonSerialize.toJSONString(rpcResponse);

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_GATEWAY, Unpooled.copiedBuffer(body, CharsetUtil.UTF_8));
        httpResponse.headers().set(CONTENT_TYPE, MediaTypeEnum.JSON.toString());
        if (null != msg) {
            httpResponse.headers().set(HEADER_REQUEST_ID, msg.headers().get(Const.HEADER_REQUEST_ID, ""));
            httpResponse.headers().set(HEADER_SERVICE_CLASS, msg.headers().get(Const.HEADER_SERVICE_CLASS, ""));
            httpResponse.headers().set(HEADER_METHOD_NAME, msg.headers().get(Const.HEADER_METHOD_NAME, ""));
        }
        httpResponse.headers().set(CONTENT_LENGTH, httpResponse.content().readableBytes());
        httpResponse.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        httpResponse.headers().set(CACHE_CONTROL, "no-cache");
        httpResponse.headers().set(PRAGMA, "no-cache");
        httpResponse.headers().set(EXPIRES, "-1");
        ctx.writeAndFlush(httpResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Server io error: {}", ctx.channel(), cause);
    }

}