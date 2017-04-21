package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.codec.RpcDecoder;
import com.kongzhong.mrpc.codec.RpcEncoder;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.transport.RpcClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author biezhi
 *         2017/4/19
 */
public class HttpClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RpcSerialize rpcSerialize;

    public HttpClientChannelInitializer(RpcSerialize rpcSerialize) {
        this.rpcSerialize = rpcSerialize;
    }

    @Override
    protected void initChannel(SocketChannel sc) throws Exception {
        sc.pipeline()
                .addLast(new RpcEncoder(rpcSerialize, RpcRequest.class))
                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, RpcSerialize.MESSAGE_LENGTH, 0, 0))
                .addLast(new RpcDecoder(rpcSerialize, RpcResponse.class))
                .addLast(new RpcClientHandler());
    }
}
