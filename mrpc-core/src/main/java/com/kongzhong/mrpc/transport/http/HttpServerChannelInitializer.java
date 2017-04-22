package com.kongzhong.mrpc.transport.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

import java.util.Map;

/**
 * Http服务端ChannelInitializer
 */
public class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String, Object> handlerMap;

    public HttpServerChannelInitializer(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                // inbound handler
                .addLast(new HttpRequestDecoder())
                .addLast(new HttpContentDecompressor())
                // outbound handler
                .addLast(new HttpResponseEncoder())
                .addLast(new HttpContentCompressor())
                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                .addLast(new HttpServerHandler(handlerMap));
    }
}
