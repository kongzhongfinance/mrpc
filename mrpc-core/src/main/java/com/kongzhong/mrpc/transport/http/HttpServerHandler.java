package com.kongzhong.mrpc.transport.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Throwables;
import com.kongzhong.mrpc.enums.MediaType;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcRet;
import com.kongzhong.mrpc.server.RpcServer;
import com.kongzhong.mrpc.utils.JSONUtils;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * http请求处理器
 *
 * @author biezhi
 *         2017/4/21
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final Logger log = LoggerFactory.getLogger(HttpServerHandler.class);

    private Map<String, Object> handlerMap;

    public HttpServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {
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
            this.sendError(ctx, RpcRet.notFound("body content not is empty."));
            return;
        }

        JSONObject reqJSON = JSON.parseObject(body);
        String serviceName = reqJSON.getString("service");
        String methodName = reqJSON.getString("method");
        String version = reqJSON.getString("version");
        JSONObject argJSON = reqJSON.getJSONObject("parameters");

        if (StringUtils.isEmpty(serviceName)) {
            this.sendError(ctx, RpcRet.notFound("[service] not is null."));
            return;
        }

        if (StringUtils.isEmpty(methodName)) {
            this.sendError(ctx, RpcRet.notFound("[method] not is null."));
            return;
        }

        log.debug("body: \n{}", body);

        Object bean = handlerMap.get(serviceName);
        if (null == bean) {
            this.sendError(ctx, RpcRet.notFound("not found [" + serviceName + "] bean."));
            return;
        }

        Class<?> type = bean.getClass();
        Method method = ReflectUtils.method(type, methodName);
        if (null == method) {
            this.sendError(ctx, RpcRet.notFound("method [" + methodName + "] not found."));
            return;
        }

        Object[] args = new Object[method.getParameterCount()];
        List<String> paramNames = ReflectUtils.getParamNames(method);
        if (null != paramNames) {

            Class<?>[] types = method.getParameterTypes();

            for (int i = 0, len = paramNames.size(); i < len; i++) {
                String paramName = paramNames.get(i);
                if (ReflectUtils.isBasic(types[i])) {
                    args[i] = argJSON.getObject(paramName, types[i]);
                } else {
                    args[i] = argJSON.get(paramName);
                }
            }
        }

        HttpResponse response = new HttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, MediaType.JSON.toString());

        RpcRequest rpcRequest = getRpcRequest(serviceName, method, args);

        HttpResponseCallback responseCallback = new HttpResponseCallback(rpcRequest, response, handlerMap);
        RpcServer.submit(responseCallback, ctx, rpcRequest);
    }

    private RpcRequest getRpcRequest(String serviceName, Method method, Object[] paramters) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(StringUtils.getUUID());
        request.setClassName(serviceName);
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
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