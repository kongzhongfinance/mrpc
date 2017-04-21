package com.kongzhong.mrpc.transport;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcRet;
import com.kongzhong.mrpc.model.ServiceMeta;
import com.kongzhong.mrpc.server.RpcServer;
import com.kongzhong.mrpc.transport.http.HttpResponse;
import com.kongzhong.mrpc.transport.http.HttpResponseCallback;
import com.kongzhong.mrpc.utils.JSONUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
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
        HttpMethod method = httpRequest.method();
        HttpHeaders headers = httpRequest.headers();
        HttpVersion httpVersion = httpRequest.protocolVersion();

        log.debug("{}", httpVersion);
        log.info("{}\t{}", method, uri);

        if (!RpcServer.serviceRouter.contains(uri)) {
            log.warn("Not Found Url: {}\t{}", method.name(), uri);
            this.sendError(ctx, RpcRet.notFound("Not Found Url"));
            return;
        }

        ServiceMeta serviceMeta = RpcServer.serviceRouter.get(uri);
        if (!serviceMeta.getHttpMethod().equals(method.name())) {
            log.warn("Request Method [{}] Not Allowed.", method.name());
            this.sendError(ctx, RpcRet.notAllowMethod("Request Method Not Allowed"));
            return;
        }

        ByteBuf buf = httpRequest.content();
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");

        log.info("headers: {}", headers);
        log.info("body: {}", body);

        //parse query parameters
        QueryStringDecoder queryDecoder = new QueryStringDecoder(uri, CharsetUtil.UTF_8);
        String path = queryDecoder.path();

        Map<String, List<String>> parameters = queryDecoder.parameters();

        //parse body parameters
        HttpPostRequestDecoder bodyDecoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(true), httpRequest);

        Map<String, String> params = Maps.newHashMap();

        List<InterfaceHttpData> datum = bodyDecoder.getBodyHttpDatas();
        if (datum != null && !datum.isEmpty()) {
            for (InterfaceHttpData data : datum) {
                String name = data.getName();
                String value = null;
                if (data.getHttpDataType().equals(InterfaceHttpData.HttpDataType.Attribute)) {
                    //do not parse file data
                    Attribute attribute = (Attribute) data;
                    try {
                        value = attribute.getString(CharsetUtil.UTF_8);
                        params.put(name, value);
                    } catch (Exception e) {
                        log.error(this.getClass().getName(), e);
                    }
                }
            }
        }
        bodyDecoder.destroy();

        HttpResponse response = new HttpResponse(HTTP_1_1,
                HttpResponseStatus.FOUND, null);

        Object[] pams = new Object[serviceMeta.getMethod().getParameterCount()];
        RpcRequest rpcRequest = getRpcRequest(serviceMeta, pams);

        // new 一个服务器消息处理线程
        HttpResponseCallback responseCallback = new HttpResponseCallback(rpcRequest, response, handlerMap);
        // 将服务端的处理任务提交给服务端的 消息处理线程池
        //不要阻塞nio线程，复杂的业务逻辑丢给专门的线程池
        RpcServer.submit(responseCallback, ctx, rpcRequest, response);

    }

    private RpcRequest getRpcRequest(ServiceMeta serviceMeta, Object[] paramters) {

        Method method = serviceMeta.getMethod();

        RpcRequest request = new RpcRequest();
        request.setRequestId(StringUtils.getUUID());
        request.setClassName(serviceMeta.getServiceName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(paramters);

        return request;
    }

    private void resp(ChannelHandlerContext ctx, String responseBody) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                HttpResponseStatus.FOUND, Unpooled.copiedBuffer(responseBody, CharsetUtil.UTF_8));

        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 错误处理
     *
     * @param ctx
     * @param status
     */
    private static void sendError(ChannelHandlerContext ctx, RpcRet ret) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(ret.getCode()), Unpooled.copiedBuffer(JSONUtils.toJSONString(ret), CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("", cause);
        ctx.close();
    }
}
