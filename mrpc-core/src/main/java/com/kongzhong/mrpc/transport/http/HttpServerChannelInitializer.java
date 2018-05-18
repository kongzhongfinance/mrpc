package com.kongzhong.mrpc.transport.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

/**
 * Http服务端ChannelInitializer
 */
public class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    public HttpServerChannelInitializer() {
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
                .addLast(new HttpServerHandler());
    }
}
