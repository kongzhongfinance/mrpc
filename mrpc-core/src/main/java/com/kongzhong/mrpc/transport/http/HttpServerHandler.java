package com.kongzhong.mrpc.transport.http;

import com.google.common.base.Throwables;
import com.kongzhong.mrpc.enums.MediaTypeEnum;
import com.kongzhong.mrpc.exception.SerializeException;
import com.kongzhong.mrpc.model.RequestBody;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcRet;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.server.RpcSpringInit;
import com.kongzhong.mrpc.transport.SimpleServerHandler;
import com.kongzhong.mrpc.utils.JSONUtils;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static com.kongzhong.mrpc.Const.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * http请求处理器
 *
 * @author biezhi
 *         2017/4/21
 */
public class HttpServerHandler extends SimpleServerHandler<FullHttpRequest> {

    public static final Logger log = LoggerFactory.getLogger(HttpServerHandler.class);

    public HttpServerHandler(Map<String, ServiceBean> serviceBeanMap) {
        super(serviceBeanMap);
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

        log.debug("body: \n\n{}\n", body);

        RequestBody requestBody = null;
        try {
            requestBody = JSONUtils.parseObject(body, RequestBody.class);
        } catch (Exception e) {
            this.sendError(ctx, RpcRet.error("unable to identify the requested format."));
            return;
        }

        String serviceName = requestBody.getService();
        String methodName = requestBody.getMethod();
        String version = requestBody.getVersion();
        List<Object> argJSON = requestBody.getParameters();

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
            this.sendError(ctx, RpcRet.notFound("Not found [" + serviceName + "] bean defined."));
            return;
        }

        Object bean = serviceBean.getBean();
        if (null == bean) {
            this.sendError(ctx, RpcRet.notFound("Not found [" + serviceName + "] bean."));
            return;
        }

        RpcRequest rpcRequest = this.parseParams(ctx, requestBody, bean.getClass());

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("", CharsetUtil.UTF_8));
        httpResponse.headers().set(CONTENT_TYPE, MediaTypeEnum.JSON.toString());
        httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, httpResponse.content().readableBytes());
        httpResponse.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        httpResponse.headers().set(HttpHeaders.Names.CACHE_CONTROL, "no-cache");
        httpResponse.headers().set(HttpHeaders.Names.PRAGMA, "no-cache");
        httpResponse.headers().set(HttpHeaders.Names.EXPIRES, "-1");
        httpResponse.headers().set(HEADER_REQUEST_ID, requestBody.getRequestId());
        httpResponse.headers().set(HEADER_SERVICE_CLASS, requestBody.getService());
        httpResponse.headers().set(HEADER_METHOD_NAME, requestBody.getMethod());

        if (HttpHeaders.isKeepAlive(httpRequest)) {
            httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        HttpResponseCallback responseCallback = new HttpResponseCallback(rpcRequest, httpResponse, serviceBeanMap);
        RpcSpringInit.submit(responseCallback, ctx);
    }

    /**
     * 解析请求参数
     *
     * @param ctx
     * @param requestBody
     * @param bean
     * @return
     * @throws NoSuchMethodException
     */
    private RpcRequest parseParams(ChannelHandlerContext ctx, RequestBody requestBody, Class<?> type) throws Exception {

        String serviceName = requestBody.getService();
        String methodName = requestBody.getMethod();

        Method method = ReflectUtils.method(type, methodName);
        List<String> parameterTypes = requestBody.getParameterTypes();
        List<Object> argJSON = requestBody.getParameters();

        // 判断根据参数列表类型查找method对象
        /*if (null != parameterTypes) {
            Class<?>[] parameterTypeArr = new Class[parameterTypes.size()];
            int pos = 0;
            for (Object parameterType : parameterTypes) {
                parameterTypeArr[pos++] = ReflectUtils.getClassType(parameterType.toString());
            }
            method = type.getMethod(methodName, parameterTypeArr);
            argJSON = requestBody.getParameters();
        } else {
            method = ReflectUtils.method(type, methodName);
        }*/

        // 找不到method
        if (null == method) {
            this.sendError(ctx, RpcRet.notFound("method [" + methodName + "] not found."));
            return null;
        }

        // 解析参数到args中
        Object[] args = new Object[method.getParameterCount()];
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        for (int i = 0; i < args.length; i++) {
            Type paramType = genericParameterTypes[i];
            args[i] = JSONUtils.parseObject(JSONUtils.toJSONString(argJSON.get(i)), paramType);
        }

        // 构造请求
        String requestId = null != requestBody.getRequestId() ? requestBody.getRequestId() : StringUtils.getUUID();
        return getRpcRequest(requestId, serviceName, method, args);
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
    private void sendError(ChannelHandlerContext ctx, RpcRet ret) throws SerializeException {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(ret.getCode()), Unpooled.copiedBuffer(JSONUtils.toJSONString(ret), CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        ctx.writeAndFlush(response)/*.addListener(ChannelFutureListener.CLOSE)*/;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Http server handler error", cause);
        sendError(ctx, RpcRet.error(Throwables.getStackTraceAsString(cause)));
//        ctx.close();
    }
}