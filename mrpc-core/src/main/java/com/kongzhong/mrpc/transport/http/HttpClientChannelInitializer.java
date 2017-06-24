package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.transport.netty.NettyClient;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

/**
 * http客户端ChannelInitializer
 */
public class HttpClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private NettyClient nettyClient;

    public HttpClientChannelInitializer(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        socketChannel.pipeline()
                // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                .addLast(new HttpResponseDecoder())
                .addLast(new HttpContentDecompressor())
                // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
                .addLast(new HttpRequestEncoder())
                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                .addLast(new HttpClientHandler(nettyClient));
    }
}
