package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.transport.HttpServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.*;

import java.util.Map;

public class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String, Object> handlerMap;

    private RpcSerialize rpcSerialize;

    public HttpServerChannelInitializer(Map<String, Object> handlerMap, RpcSerialize rpcSerialize) {
        this.handlerMap = handlerMap;
        this.rpcSerialize = rpcSerialize;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        HttpServerHandler httpServerHandler = new HttpServerHandler(handlerMap);

        socketChannel.pipeline()
                // inbound handler
                .addLast(new HttpRequestDecoder())
                .addLast(new HttpContentDecompressor())
                // outbound handler
                .addLast(new HttpResponseEncoder())
                .addLast(new HttpContentCompressor())
                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                .addLast(httpServerHandler);
    }
}
