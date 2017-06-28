package com.kongzhong.mrpc.transport.tcp;

import com.kongzhong.mrpc.serialize.RpcDecoder;
import com.kongzhong.mrpc.serialize.RpcEncoder;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.server.RpcMapping;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Tcp服务端ChannelInitializer
 */
public class TcpServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RpcSerialize rpcSerialize;

    public TcpServerChannelInitializer(RpcSerialize rpcSerialize) {
        this.rpcSerialize = rpcSerialize;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, RpcSerialize.MESSAGE_LENGTH, 0, 0))
                .addLast(new RpcDecoder(rpcSerialize, RpcRequest.class))
                .addLast(new RpcEncoder(rpcSerialize, RpcResponse.class))
                .addLast(new TcpServerHandler(RpcMapping.me().getServiceBeanMap()));
    }
}
