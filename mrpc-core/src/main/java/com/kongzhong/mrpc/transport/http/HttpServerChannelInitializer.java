package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.server.RpcMapping;
import com.kongzhong.mrpc.transport.netty.TelnetServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;

/**
 * Http服务端ChannelInitializer
 */
public class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();

    private static final TelnetServerHandler SERVER_HANDLER = new TelnetServerHandler();

    public HttpServerChannelInitializer() {
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
                .addLast(DECODER)
                .addLast(ENCODER)
                .addLast(SERVER_HANDLER)
                // inbound handler
                .addLast(new HttpRequestDecoder())
                .addLast(new HttpContentDecompressor())
                // outbound handler
                .addLast(new HttpResponseEncoder())
                .addLast(new HttpContentCompressor())
                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                .addLast(new HttpServerHandler(RpcMapping.me().getServiceBeanMap()));
    }
}
